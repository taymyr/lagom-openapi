package org.taymyr.lagom.openapi.internal

import akka.NotUsed
import com.google.common.reflect.TypeToken
import com.lightbend.lagom.internal.javadsl.api.MethodRefServiceCallHolder
import com.lightbend.lagom.javadsl.api.Descriptor.Call
import com.lightbend.lagom.javadsl.api.Descriptor.NamedCallId
import com.lightbend.lagom.javadsl.api.Descriptor.PathCallId
import com.lightbend.lagom.javadsl.api.Descriptor.RestCallId
import com.lightbend.lagom.javadsl.api.Descriptor.ServiceCallHolder
import com.lightbend.lagom.javadsl.api.Service
import com.lightbend.lagom.javadsl.api.ServiceCall
import io.swagger.v3.core.util.AnnotationsUtils
import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.links.Link
import io.swagger.v3.oas.models.media.Schema
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import io.swagger.v3.oas.models.ExternalDocumentation as ModelExternalDocumentation
import io.swagger.v3.oas.models.Operation as ModelOperation
import io.swagger.v3.oas.models.security.SecurityRequirement as ModelSecurityRequirement
import io.swagger.v3.oas.models.servers.Server as ModelServer
import io.swagger.v3.oas.models.tags.Tag as ModelTag

class ServiceReader(private val spec: OpenAPI = OpenAPI()) {

    private var classSecurityRequirements: List<ModelSecurityRequirement> = emptyList()
    private var classTags: List<String> = emptyList()
    private var classServers: List<ModelServer> = emptyList()
    private var classExternalDocumentation: ModelExternalDocumentation? = null

    fun read(service: Service): OpenAPI {
        val serviceClass = service.javaClass
        if (serviceClass.isAnnotationPresent(Hidden::class.java)) return spec

        beforeRead()
        readDefinition(serviceClass)
        readSecuritySchemes(serviceClass)

        readClassSecurityRequirements(serviceClass)
        readClassTags(serviceClass)
        readClassServers(serviceClass)
        readClassExternalDocumentation(serviceClass)

        readServiceCalls(service)
        afterRead()
        return spec
    }

    private fun beforeRead() {
        spec.components = (spec.components ?: Components()).schemas(mutableMapOf()).links(mutableMapOf())
        spec.paths = Paths()
    }

    private fun afterRead() {
        spec.components.links?.ifEmpty { spec.components.links(null) }
    }

    private fun readDefinition(clazz: Class<Service>) {
        clazz.getAnnotationInherited(OpenAPIDefinition::class.java)?.let { def ->
            spec.info = AnnotationsUtils.getInfo(def.info).orElse(null)
            spec.externalDocs = AnnotationsUtils.getExternalDocumentation(def.externalDocs).orElse(null)
            spec.servers = AnnotationsUtils.getServers(def.servers).orElse(null)
            spec.security = def.security.model
            spec.extensions = AnnotationsUtils.getExtensions(*def.extensions).ifEmpty { null }
            AnnotationsUtils.getTags(def.tags, false).ifPresent { tags -> tags.forEach { spec.addTagsItem(it) } }
        }
    }

    private fun readSecuritySchemes(clazz: Class<Service>) {
        clazz.getAnnotationsInherited(SecurityScheme::class.java)?.let { schemas ->
            spec.components.securitySchemes = spec.components.securitySchemes ?: mutableMapOf()
            spec.components.securitySchemes.putAll(schemas.map { it.model }.associateBy({ it.first }, { it.second }))
        }
    }

    private fun readClassSecurityRequirements(clazz: Class<Service>) {
        classSecurityRequirements = clazz.getAnnotationsInherited(SecurityRequirement::class.java)?.toTypedArray()?.model ?: emptyList()
    }

    private fun readClassTags(clazz: Class<Service>) {
        classTags = AnnotationsUtils.getTags(clazz.getAnnotationsInherited(Tag::class.java)?.toTypedArray(), false).orElse(emptySet())
            .map { it.name }
    }

    private fun readClassServers(clazz: Class<Service>) {
        classServers = AnnotationsUtils.getServers(clazz.getAnnotationsInherited(Server::class.java)?.toTypedArray()).orElse(emptyList())
    }

    private fun readClassExternalDocumentation(clazz: Class<Service>) {
        classExternalDocumentation =
            AnnotationsUtils.getExternalDocumentation(clazz.getAnnotationInherited(ExternalDocumentation::class.java)).orElse(null)
    }

    private fun readServiceCalls(service: Service) = service.descriptor().calls().asSequence()
        .filter { isNotHidden(it.serviceCallHolder()) }
        .forEach { readServiceCall(it) }

    private fun readMethodTags(method: Method): List<ModelTag> =
        AnnotationsUtils.getTags(method.getAnnotationsInherited(Tag::class.java)?.toTypedArray(), true).orElse(emptySet()).toList()

    private fun readMethodServers(method: Method): List<ModelServer> =
        AnnotationsUtils.getServers(method.getAnnotationsInherited(Server::class.java)?.toTypedArray()).orElse(emptyList()).toList()

    private fun readMethodRefServiceCallHolder(call: Call<*, *>): PathOperation {
        val method = (call.serviceCallHolder() as MethodRefServiceCallHolder).toMethod()
        val operationModel = method.getAnnotationInherited(Operation::class.java)!!.model
        val operation = operationModel.underlying!!

        classSecurityRequirements.forEach { operation.addSecurityItem(it) }
        classServers.forEach { operation.addServersItem(it) }
        readMethodServers(method).forEach { operation.addServersItem(it) }
        operation.externalDocs = operation.externalDocs ?: classExternalDocumentation
        readMethodTags(method).forEach { operation.addTagsItem(it.name) }
        classTags.forEach { operation.addTagsItem(it) }
        operation.tags = operation.tags?.distinct()

        return PathOperation(call.opeapiPath, httpMethod(call, method), operation, operationModel.schemas, operationModel.links)
    }

    private fun readServiceCall(call: Call<*, *>) {
        val pathOperation = when (call.serviceCallHolder()) {
            is MethodRefServiceCallHolder -> readMethodRefServiceCallHolder(call)
            else -> throw IllegalArgumentException(
                "Undefined type of ServiceCallHolder, only MethodRefServiceCallHolder is supported at the moment"
            )
        }
        spec.paths.getOrPut(pathOperation.path, { PathItem() }).operation(pathOperation.httpMethod, pathOperation.operation)
        pathOperation.schemas.forEach { spec.components.schemas.putIfAbsent(it.key, it.value) }
        pathOperation.links.forEach { spec.components.links.putIfAbsent(it.key, it.value) }
    }

    private fun isNotHidden(callHolder: ServiceCallHolder): Boolean = when (callHolder) {
        is MethodRefServiceCallHolder -> {
            val method = callHolder.toMethod()
            val operation = method.getAnnotationInherited(Operation::class.java)
            val hidden = method.getAnnotationInherited(Hidden::class.java)
            operation != null && !operation.hidden && hidden == null
        }
        else -> false
    }

    private val Call<*, *>.opeapiPath: String get() {
        return when (val callId = callId()) {
            is RestCallId -> openapiPath(callId.pathPattern())
            is PathCallId -> openapiPath(callId.pathPattern())
            is NamedCallId -> "/${callId.name()}"
            else -> throw IllegalArgumentException("${callId::class.java} is not supported")
        }
    }

    private fun openapiPath(path: String): String =
        path.substringBefore("?").split("/").joinToString("/") { part ->
            if (part.startsWith(":")) "{${part.substring(1)}}" else part
        }

    private fun httpMethod(call: Call<*, *>, method: Method): HttpMethod = when (val callId = call.callId()) {
        is RestCallId -> HttpMethod.valueOf(callId.method().name())
        else -> if (hasRequestBody(method)) HttpMethod.POST else HttpMethod.GET
    }

    private fun hasRequestBody(method: Method): Boolean {
        @Suppress("UNCHECKED_CAST", "UnstableApiUsage")
        val serviceCallType = (TypeToken.of(method.genericReturnType) as TypeToken<ServiceCall<*, *>>)
            .getSupertype(ServiceCall::class.java)
            .type as? ParameterizedType ?: throw IllegalStateException("ServiceCall is not a parameterized type?")
        if (serviceCallType.actualTypeArguments.size != 2) throw IllegalStateException("ServiceCall does not have 2 type arguments?")
        if (method.returnType != ServiceCall::class.java) throw IllegalArgumentException("Service calls must return ServiceCall, subtypes are not allowed")
        return serviceCallType.actualTypeArguments[0] != NotUsed::class.java
    }

    data class PathOperation(
        val path: String,
        val httpMethod: HttpMethod,
        val operation: ModelOperation,
        val schemas: Map<String, Schema<Any>>,
        val links: Map<String, Link>
    )
}