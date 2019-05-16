package org.taymyr.lagom.internal.openapi

import io.swagger.v3.core.util.AnnotationsUtils
import io.swagger.v3.core.util.ParameterProcessor
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.servers.Server
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import kotlin.collections.forEach
import io.swagger.v3.oas.models.Operation as ModelOperation
import io.swagger.v3.oas.models.parameters.RequestBody as ModelRequestBody
import io.swagger.v3.oas.models.responses.ApiResponse as ModelApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses as ModelApiResponses

/**
 * Processor for OpenAPI method level annotations.
 */
object OperationMethodProcessor {

    /**
     * Processing annotation [SecurityRequirement] on method level.
     * Merging them with security on class level.
     */
    private fun processSecurityRequirements(security: Array<SecurityRequirement>, classInfo: ClassLevelInfo, operation: ModelOperation) {
        security.forEach { operation.addSecurityItem(it.model) }
        classInfo.securityRequirements.forEach { operation.addSecurityItem(it.model) }
    }

    /**
     * Processing annotation [Tag] on method level.
     * Merging them with tags on class level and tags from [Operation] annotation.
     */
    private fun processTags(tags: Array<String>, call: LagomCallInfo, classInfo: ClassLevelInfo, spec: OpenAPI, operation: ModelOperation) {
        tags.forEach { operation.addTagsItem(it) }
        val methodTags = call.method.getAnnotationsInherited(Tag::class.java)?.toTypedArray()
        AnnotationsUtils.getTags(classInfo.tags.toTypedArray(), false).orElse(emptySet()).forEach { tag -> operation.addTagsItem(tag.name) }
        AnnotationsUtils.getTags(methodTags, false).orElse(emptySet()).forEach { tag -> operation.addTagsItem(tag.name) }

        AnnotationsUtils.getTags(classInfo.tags.toTypedArray(), true).orElse(emptySet()).forEach { tag ->
            spec.tags.removeIf { it.name == tag.name }
            spec.addTagsItem(tag)
        }
        AnnotationsUtils.getTags(methodTags, true).orElse(emptySet()).forEach { tag ->
            spec.tags.removeIf { it.name == tag.name }
            spec.addTagsItem(tag)
        }
        operation.tags = operation.tags?.distinct()
    }

    /**
     * Processing annotation [Server] on method level.
     * Merging them with servers on class level and servers from [Operation] annotation.
     */
    private fun processServers(servers: Array<Server>, call: LagomCallInfo, classInfo: ClassLevelInfo, operation: ModelOperation) {
        AnnotationsUtils.getServers(servers).ifPresent { it.forEach { server -> operation.addServersItem(server) } }
        AnnotationsUtils.getServers(call.method.getAnnotationsInherited(Server::class.java)?.toTypedArray())
            .ifPresent { it.forEach { server -> operation.addServersItem(server) } }
        AnnotationsUtils.getServers(classInfo.servers.toTypedArray())
            .ifPresent { it.forEach { server -> operation.addServersItem(server) } }
    }

    /**
     * Processing annotation [RequestBody] on method level.
     */
    private fun processRequestBody(requestBody: RequestBody, spec: OpenAPI, operation: ModelOperation) =
        operation.requestBody(
            if (requestBody.ref.isBlank() && requestBody.content.isEmpty()) null
            else ModelRequestBody()
                .description(requestBody.description.ifBlank { null })
                .required(if (requestBody.required) requestBody.required else null)
                .content(AnnotationsUtils.getContent(
                    requestBody.content,
                    null,
                    null,
                    null,
                    spec.components,
                    null
                ).orElse(null))
                .`$ref`(requestBody.ref.ifBlank { null })
        )

    /**
     * Processing array of annotations [Parameter] on method level.
     */
    private fun processParameters(parameters: Array<Parameter>, spec: OpenAPI, operation: ModelOperation) =
        parameters.forEach { parameter ->
            operation.addParametersItem(
                ParameterProcessor.applyAnnotations(
                    null,
                    ParameterProcessor.getParameterType(parameter),
                    listOf(parameter),
                    spec.components,
                    arrayOf(),
                    arrayOf(),
                    null
                )
            )
        }

    /**
     * Processing array of annotations [ApiResponse] on method level.
     */
    private fun processApiResponses(responses: Array<ApiResponse>, call: LagomCallInfo, spec: OpenAPI, operation: ModelOperation) {
        val allResponses: MutableList<ApiResponse> = mutableListOf()
        allResponses.addAll(responses)
        call.method.getAnnotationsInherited(ApiResponse::class.java)?.let { allResponses.addAll(it) }
        allResponses.map { response ->
            response.responseCode to ModelApiResponse()
                .description(response.description)
                .headers(AnnotationsUtils.getHeaders(response.headers, null).orElse(null))
                .content(AnnotationsUtils.getContent(
                    response.content,
                    null,
                    null,
                    null,
                    spec.components,
                    null
                ).orElse(null))
                .`$ref`(response.ref.ifBlank { null })
                .apply {
                    links = AnnotationsUtils.getLinks(response.links).ifEmpty { null }
                }
        }.forEach { operation.responses.addApiResponse(it.first, it.second) }
    }

    /**
     * Processing annotation [Operation] on method level.
     */
    private fun processOperation(call: LagomCallInfo, classInfo: ClassLevelInfo, spec: OpenAPI, operation: ModelOperation) {
        operation.responses = operation.responses ?: ModelApiResponses()
        call.method.getAnnotationInherited(Operation::class.java)?.let { annotation ->
            operation.description(annotation.description.ifBlank { null })
            operation.operationId(annotation.operationId.ifBlank { null })
            operation.summary(annotation.summary.ifBlank { null })
            operation.externalDocs(
                AnnotationsUtils.getExternalDocumentation(annotation.externalDocs)
                    .orElse(AnnotationsUtils.getExternalDocumentation(classInfo.externalDocumentation)
                        .orElse(null))
            )
            processParameters(annotation.parameters, spec, operation)
            processRequestBody(annotation.requestBody, spec, operation)
            processApiResponses(annotation.responses, call, spec, operation)
            processServers(annotation.servers, call, classInfo, operation)
            processTags(annotation.tags, call, classInfo, spec, operation)
            processSecurityRequirements(annotation.security, classInfo, operation)
        }
        operation.operationId = operation.operationId ?: call.method.name
    }

    /**
     * Processing OpenAPI annotations on class level.
     * @param call Class of service
     * @param classInfo Data on class level
     * @param spec OpenAPI specification. *Note:* to be filled while processing.
     */
    fun process(call: LagomCallInfo, classInfo: ClassLevelInfo, spec: OpenAPI) {
        if (!call.isHidden()) {
            val operation = ModelOperation()
            processOperation(call, classInfo, spec, operation)
            spec.paths.getOrPut(call.path, { PathItem() })
                .operation(PathItem.HttpMethod.valueOf(call.httpMethod), operation)
        }
    }
}