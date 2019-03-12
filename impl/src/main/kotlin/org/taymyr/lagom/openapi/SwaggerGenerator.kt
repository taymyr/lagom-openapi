package org.taymyr.lagom.openapi

import akka.NotUsed
import com.lightbend.lagom.internal.javadsl.api.MethodRefServiceCallHolder
import com.lightbend.lagom.javadsl.api.Descriptor
import com.lightbend.lagom.javadsl.api.ServiceCall
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.parameters.PathParameter
import io.swagger.v3.oas.models.parameters.QueryParameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import mu.KLogging
import org.taymyr.lagom.openapi.Utils.Companion.modelConverter
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.util.Optional

class SwaggerGenerator(private val service: OpenAPIService) {

    companion object : KLogging()

    private var openApi: OpenAPI? = null

    private fun generate(): OpenAPI {
        if (openApi != null) return openApi!!
        val sd = service.javaClass.getAnnotationInherited(OpenAPIDefinition::class.java)
        var openAPINN = OpenAPI()
        openApi = openAPINN
        sd?.run {
            // TODO: security, extensions
            openAPINN = readDescriptor(openAPINN
                .info(sd.info.toModel())
                .tags(sd.tags.toModel())
                .servers(sd.servers.toModel())
                .externalDocs(sd.externalDocs.toModel())
            )
        }
        return openAPINN
    }

    private fun readDescriptor(openAPI: OpenAPI): OpenAPI {
        val descriptor = service.descriptor()
        openAPI.paths = openAPI.paths ?: Paths()
        openAPI.components = openAPI.components ?: Components()

        descriptor.calls().forEach { call -> run {
            val callId = call.callId()
            when (callId) {
                is Descriptor.RestCallId -> {
                    var operation = Operation()

                    val callHolder = call.serviceCallHolder()
                    if (callHolder is MethodRefServiceCallHolder) {
                        val method = callHolder.toMethod()
                        if (isValidMethod(method)) {
                            if (method.isAnnotationPresent(io.swagger.v3.oas.annotations.Operation::class.java)) {
                                val operationAnnotation = method.getAnnotation(io.swagger.v3.oas.annotations.Operation::class.java)
                                operation = operationAnnotation.toModel(openAPI)
                            }

                            operation.responses = operation.responses ?: ApiResponses()
                            operation.operationId = operation.operationId ?: method.name

                            val models = method.readModels()
                            processRequest(openAPI, operation, models[0])
                            processResponse(openAPI, operation, models[1])
                            processParameters(operation, method, callId.listOfPathParams(), callId.listOfQueryParams())
                        } else {
                            logger.error { "Method ${method.name} is not valid." }
                            return@run
                        }
                    }

                    val swaggerPath = callId.swaggerTemplatedPath()
                    val path = if (openAPI.paths.containsKey(swaggerPath)) openAPI.paths[swaggerPath]!! else PathItem()
                    path.operation(PathItem.HttpMethod.valueOf(callId.method().name()), operation)
                    openAPI.paths[swaggerPath] = path
                }
                is Descriptor.NamedCallId -> {
                    logger.warn { "NamedCallId is not supported now." }
                }
                is Descriptor.PathCallId -> {
                    logger.warn { "PathCallId is not supported now." }
                }
                else -> {
                    logger.warn { "Undefined type of CallId." }
                }
            }
        } }

        return openAPI
    }

    private fun isValidMethod(method: Method): Boolean {
        if (method.genericReturnType is ParameterizedType) {
            val returnType = method.genericReturnType as ParameterizedType
            if (returnType.rawType is Class<*>) {
                val clazz = returnType.rawType as Class<*>
                if (clazz.isAssignableFrom(ServiceCall::class.java)) {
                    if (method.readModels().size != 2) {
                        logger.error { "Can't read request/response models" }
                    } else {
                        return true
                    }
                } else {
                    logger.error { "Method ${method.name} return type is not a ServiceCall" }
                }
            }
        }
        return false
    }

    private fun processRequest(openAPI: OpenAPI, operation: Operation, model: TypeModel) {
        if (!model.cls.isAssignableFrom(NotUsed::class.java) && operation.requestBody == null) {
            val requestResolvedSchema = modelConverter.resolveAsResolvedSchema(model.cls)
            requestResolvedSchema.referencedSchemas.forEach { name, schema -> run {
                if (openAPI.components.schemas == null || !openAPI.components.schemas.containsKey(name)) {
                    openAPI.components.addSchemas(name, schema)
                }
            } }
            operation.requestBody = RequestBody()
                .description(requestResolvedSchema.schema.name)
                .content(Content()
                    .addMediaType("application/json", MediaType()
                        .schema(RefSchema(requestResolvedSchema.schema.name))))
        }
    }

    private fun processResponse(openAPI: OpenAPI, operation: Operation, model: TypeModel) {
        if (operation.responses["200"] == null && operation.responses.isEmpty()) {
            val responseResolvedSchema = modelConverter.resolveAsResolvedSchema(model.cls)
            responseResolvedSchema.referencedSchemas.forEach { name, schema -> run {
                if (openAPI.components.schemas == null || !openAPI.components.schemas.containsKey(name)) {
                    openAPI.components.addSchemas(name, schema)
                }
            } }
            val refSchema = if (model.isArray)
                ArraySchema().items(RefSchema(responseResolvedSchema.schema.name))
            else
                RefSchema(responseResolvedSchema.schema.name)
            operation.responses["200"] = ApiResponse()
                .description(responseResolvedSchema.schema.name)
                .content(Content()
                    .addMediaType("application/json", MediaType()
                        .schema(refSchema)))
        }
    }

    private fun processParameters(operation: Operation, method: Method, pathParamsList: List<String>, queryParamsList: List<String>) {
        var pathParamsListSize = pathParamsList.size
        var queryParamsListSize = queryParamsList.size
        method.parameters.forEach { parameter -> run {
            val parameterModels = parameter.annotatedType.type.readModels()
            if (parameterModels.size == 1) {
                var schema = modelConverter.resolveAsResolvedSchema(AnnotatedType()
                    .type(parameterModels[0].cls)).schema
                if (parameterModels[0].isArray) {
                    schema = ArraySchema().items(schema)
                }
                if (pathParamsListSize > 0) {
                    val name = pathParamsList[pathParamsList.size - pathParamsListSize--]
                    val p = operation.findParameterByName(name)
                    if (p == null) {
                        operation.addParametersItem(PathParameter()
                            .schema(schema)
                            .name(name))
                    } else {
                        if (p.schema == null && p.`$ref`.isNullOrBlank()) {
                            p.schema = schema
                        }
                    }
                } else if (queryParamsListSize > 0) {
                    val name = queryParamsList[queryParamsList.size - queryParamsListSize--]
                    val p = operation.findParameterByName(name)
                    if (p == null) {
                        operation.addParametersItem(QueryParameter()
                            .schema(schema)
                            .name(name)
                            .required(parameter.type.isAssignableFrom(Optional::class.java)))
                    } else {
                        if (p.schema == null && p.`$ref`.isNullOrBlank()) {
                            p.schema = schema
                            p.required = parameter.type.isAssignableFrom(Optional::class.java)
                        }
                    }
                }
            }
        } }
    }

    fun generateYaml() = toYaml(generate())

    private fun toYaml(swagger: OpenAPI) = Yaml.pretty(swagger)!!
}
