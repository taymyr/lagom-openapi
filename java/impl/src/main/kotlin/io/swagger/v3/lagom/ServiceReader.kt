package io.swagger.v3.lagom

import com.lightbend.lagom.internal.javadsl.api.MethodRefServiceCallHolder
import com.lightbend.lagom.javadsl.api.Descriptor
import com.lightbend.lagom.javadsl.api.Service
import io.swagger.v3.core.util.AnnotationsUtils
import io.swagger.v3.core.util.ReflectionUtils
import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import org.apache.commons.lang3.StringUtils
import java.lang.reflect.Method
import java.util.Optional

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
class ServiceReader private constructor(
    private val openAPI: OpenAPI,
    private val paths: Paths,
    private val openApiTags: LinkedHashSet<Tag>,
    private val components: Components,
    private val securityParser: SecurityParser,
    private val serviceCallReader: ServiceCallReader
) {

    constructor(openAPI: OpenAPI) : this(openAPI, Paths(), LinkedHashSet<Tag>(), Components(), SecurityParser(), ServiceCallReader())

    constructor() : this(OpenAPI())

    fun read(service: Service): OpenAPI {

        val cls = service.javaClass

        if (cls.getAnnotation(Hidden::class.java) != null) {
            return this.openAPI
        }

        openAPI.paths = openAPI.paths ?: Paths()
        openAPI.components = openAPI.components ?: Components()

        readOpenAPIDefinition(cls)
        readClassSecuritySchemes(cls)

        val classSecurityRequirements = readClassSecurityRequirements(cls)
        val classTags = readClassTags(cls)
        val classServers = readClassServers(cls)
        val classExternalDocumentation = AnnotationsUtils.getExternalDocumentation(
            ReflectionUtils.getAnnotation<ExternalDocumentation>(cls, ExternalDocumentation::class.java)
        )
        service.descriptor().calls()
            .filter { call -> call.serviceCallHolder() is MethodRefServiceCallHolder }
            .filter { call -> !isToSkip((call.serviceCallHolder() as MethodRefServiceCallHolder).toMethod()) }
            .forEach { call -> readServiceCall(
                call.callId(), call.serviceCallHolder(),
                classSecurityRequirements, classTags, classServers, classExternalDocumentation
            ) }
        openAPI.tags(openApiTags.toList())
        return openAPI
    }

    private fun readServiceCall(
        callId: Descriptor.CallId,
        callHolder: Descriptor.ServiceCallHolder,
        classSecurityRequirements: List<SecurityRequirement>,
        classTags: Set<String>,
        classServers: List<Server>,
        classExternalDocumentation: Optional<io.swagger.v3.oas.models.ExternalDocumentation>
    ) {
        val parseResult = serviceCallReader.read(
            ServiceCallReader.Call(callId, callHolder),
            ServiceCallReader.Context(securityParser, classSecurityRequirements, classTags, classServers, classExternalDocumentation)
        )

        val path = if (openAPI.paths.containsKey(parseResult.path)) openAPI.paths[parseResult.path]!! else PathItem()
        path.operation(parseResult.httpMethod, parseResult.operation)
        openAPI.paths[parseResult.path] = path

        parseResult.referencedSchemas.forEach { (name, schema) -> run {
            if (openAPI.components.schemas == null || !openAPI.components.schemas.containsKey(name)) {
                openAPI.components.addSchemas(name, schema)
            }
        } }
    }

    private fun isToSkip(method: Method): Boolean {
        val apiOperation = ReflectionUtils.getAnnotation(method, io.swagger.v3.oas.annotations.Operation::class.java)
        if (apiOperation == null || apiOperation.hidden) {
            return true
        }
        val hidden = method.getAnnotation(Hidden::class.java)
        if (hidden != null) {
            return true
        }
        return false
    }

    private fun readClassServers(cls: Class<Service>): List<Server> {
        val apiServers = ReflectionUtils.getRepeatableAnnotationsArray(
            cls, io.swagger.v3.oas.annotations.servers.Server::class.java
        )
        val classServers: List<Server>
        if (apiServers != null) {
            classServers = ArrayList<Server>()
            AnnotationsUtils.getServers(apiServers).ifPresent { servers -> classServers.addAll(servers) }
        } else {
            classServers = emptyList()
        }
        return classServers
    }

    private fun readClassTags(cls: Class<Service>): Set<String> {
        val apiTags = ReflectionUtils.getRepeatableAnnotationsArray(
            cls, io.swagger.v3.oas.annotations.tags.Tag::class.java
        )
        val classTags: Set<String>
        if (apiTags != null) {
            classTags = java.util.LinkedHashSet<String>()
            AnnotationsUtils.getTags(apiTags, false).ifPresent {
                tags -> tags.stream().map { t -> t.name }.forEach { t -> classTags.add(t) }
            }
        } else {
            classTags = emptySet()
        }
        return classTags
    }

    private fun readClassSecurityRequirements(cls: Class<Service>): List<SecurityRequirement> {
        val apiSecurityRequirements = ReflectionUtils.getRepeatableAnnotations<io.swagger.v3.oas.annotations.security.SecurityRequirement>(
            cls, io.swagger.v3.oas.annotations.security.SecurityRequirement::class.java
        )
        val classSecurityRequirements: List<SecurityRequirement>
        if (apiSecurityRequirements != null) {
            val requirementsObject = securityParser.getSecurityRequirements(
                apiSecurityRequirements.toTypedArray()
            )
            if (requirementsObject.isPresent) {
                classSecurityRequirements = requirementsObject.get()
            } else {
                classSecurityRequirements = emptyList()
            }
        } else {
            classSecurityRequirements = emptyList()
        }
        return classSecurityRequirements
    }

    private fun readOpenAPIDefinition(cls: Class<Service>) {

        val def = ReflectionUtils.getAnnotation(cls, OpenAPIDefinition::class.java)

        if (def != null) {

            AnnotationsUtils.getInfo(def.info).ifPresent { info -> openAPI.info = info }

            SecurityParser()
                .getSecurityRequirements(def.security)
                .ifPresent { s -> openAPI.security = s }

            AnnotationsUtils
                .getExternalDocumentation(def.externalDocs)
                .ifPresent { docs -> openAPI.externalDocs = docs }

            AnnotationsUtils
                .getTags(def.tags, false)
                .ifPresent { tags -> openApiTags.addAll(tags) }

            AnnotationsUtils.getServers(def.servers).ifPresent { servers -> openAPI.servers = servers }

            if (def.extensions.isNotEmpty()) {
                openAPI.extensions = AnnotationsUtils
                    .getExtensions(*def.extensions)
            }
        }
    }

    private fun readClassSecuritySchemes(cls: Class<Service>) {
        val apiSecurityScheme = ReflectionUtils.getRepeatableAnnotations<io.swagger.v3.oas.annotations.security.SecurityScheme>(
            cls, io.swagger.v3.oas.annotations.security.SecurityScheme::class.java
        )
        if (apiSecurityScheme != null) {
            for (securitySchemeAnnotation in apiSecurityScheme) {
                val securityScheme = securityParser.getSecurityScheme(securitySchemeAnnotation)
                if (securityScheme.isPresent) {
                    val securitySchemeMap = HashMap<String, SecurityScheme>()
                    if (StringUtils.isNotBlank(securityScheme.get().key)) {
                        securitySchemeMap[securityScheme.get().key] = securityScheme.get().securityScheme
                        if (components.securitySchemes != null && components.securitySchemes.isNotEmpty()) {
                            components.securitySchemes.putAll(securitySchemeMap)
                        } else {
                            components.securitySchemes = securitySchemeMap
                        }
                    }
                }
            }
        }
    }
}