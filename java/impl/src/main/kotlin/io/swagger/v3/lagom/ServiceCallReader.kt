package io.swagger.v3.lagom

import com.google.common.base.Joiner
import com.lightbend.lagom.internal.javadsl.api.MethodRefServiceCallHolder
import com.lightbend.lagom.javadsl.api.Descriptor
import io.swagger.v3.core.util.AnnotationsUtils
import io.swagger.v3.core.util.ReflectionUtils
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import mu.KLogging
import java.lang.IllegalArgumentException
import java.lang.reflect.Method
import java.util.Optional
import java.util.function.Consumer

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
class ServiceCallReader {
    companion object : KLogging()

    fun read(call: Call, context: Context): ParseResult {
        when (call.serviceCall) {
            is MethodRefServiceCallHolder -> {
                return readMethodRefServiceCallHolder(call, context)
            }
            else -> {
                throw IllegalArgumentException(
                    "Undefined type of ServiceCallHolder, only MethodRefServiceCallHolder is supported at the moment"
                )
            }
        }
    }

    private fun readMethodRefServiceCallHolder(call: Call, context: Context): ParseResult {

        val method = (call.serviceCall as MethodRefServiceCallHolder).toMethod()

        val apiOperation = ReflectionUtils.getAnnotation<io.swagger.v3.oas.annotations.Operation>(
            method, io.swagger.v3.oas.annotations.Operation::class.java
        )

        val operationModel = apiOperation.toModel()
        val operation = operationModel.model!!

        context.classSecurityRequirements.forEach { operation.addSecurityItem(it) }
        context.classServers.forEach { operation.addServersItem(it) }
        if (operation.externalDocs == null) {
            context.classExternalDocumentation.ifPresent { operation.externalDocs = it }
        }

        return ParseResult(
            definePath(call), defineHttpMethod(call), operation,
            operationModel.referencedSchemas
        )
    }

    private fun readMethodApiTags(method: Method, operation: Operation): Set<Tag> {
        val apiTags = ReflectionUtils.getRepeatableAnnotations<io.swagger.v3.oas.annotations.tags.Tag>(
            method, io.swagger.v3.oas.annotations.tags.Tag::class.java
        )
        val openApiTags = mutableSetOf<Tag>()
        if (apiTags != null) {
            apiTags.stream()
                .filter { t -> operation.tags == null || operation.tags != null && !operation.tags.contains(t.name) }
                .map<String> { t -> t.name }
                .forEach(Consumer<String> { operation.addTagsItem(it) })
            AnnotationsUtils.getTags(apiTags.toTypedArray(), true).ifPresent { tags -> openApiTags.addAll(tags) }
        }
        return openApiTags
    }

    private fun definePath(call: Call): String {
        return when (call.id) {
            is Descriptor.RestCallId -> call.id.swaggerTemplatedPath()
            is Descriptor.PathCallId -> call.id.swaggerTemplatedPath()
            is Descriptor.NamedCallId -> "/" + call.id.name()
            else -> throw IllegalArgumentException("Such call id is not supported: " + call.id::class.java)
        }
    }

    private fun Descriptor.RestCallId.swaggerTemplatedPath(): String {
        return convertToSwaggerPath(this.pathPattern())
    }

    private fun Descriptor.PathCallId.swaggerTemplatedPath(): String {
        return convertToSwaggerPath(this.pathPattern())
    }

    private fun convertToSwaggerPath(pathPattern: String): String {
        return Joiner.on("/").join(
            pathWithoutQueryParams(pathPattern).split("/").map { segment ->
                if (segment.startsWith(":")) "{" + segment.substring(1) + "}" else segment
            }
        )
    }

    private fun pathWithoutQueryParams(pathPattern: String): String {
        return if (pathPattern.contains("?")) pathPattern.substring(0, pathPattern.indexOf("?")) else pathPattern
    }

    private fun defineHttpMethod(call: Call): PathItem.HttpMethod {
        // When you use call, namedCall or pathCall, if Lagom maps that down to REST,
        // Lagom will make a best effort attempt to map it down to REST in a semantic fashion,
        // so that means if there is a request message, it will use POST, if there’s none, it will use GET.
        return when (call.id) {
            is Descriptor.RestCallId -> PathItem.HttpMethod.valueOf(call.id.method().name())
            else -> if (hasRequest(call)) PathItem.HttpMethod.POST else PathItem.HttpMethod.GET
        }
    }

    private fun hasRequest(call: Call): Boolean {
        // TODO
        return true
    }

    data class Call(val id: Descriptor.CallId, val serviceCall: Descriptor.ServiceCallHolder)

    data class ParseResult(
        val path: String,
        val httpMethod: PathItem.HttpMethod,
        val operation: Operation,
        val referencedSchemas: Map<String, Schema<Any>>
    )

    data class Context(
        val securityParser: SecurityParser,
        val classSecurityRequirements: List<SecurityRequirement>,
        val classTags: Set<String>,
        val classServers: List<Server>,
        val classExternalDocumentation: Optional<ExternalDocumentation>
    )
}