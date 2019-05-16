package org.taymyr.lagom.internal.openapi

import io.swagger.v3.core.util.AnnotationsUtils
import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.models.OpenAPI

/**
 * Processor for OpenAPI class level annotations.
 */
object ServiceClassProcessor {

    /**
     * Processing annotation [OpenAPIDefinition] on class level.
     */
    private fun processOpenAPIDefinition(clazz: Class<*>, spec: OpenAPI) {
        clazz.getAnnotationInherited(OpenAPIDefinition::class.java)?.let { def ->
            spec.info = AnnotationsUtils.getInfo(def.info).orElse(null)
            spec.externalDocs = AnnotationsUtils.getExternalDocumentation(def.externalDocs).orElse(null)
            spec.servers = AnnotationsUtils.getServers(def.servers).orElse(null)
            spec.security = def.security.model
            spec.extensions = AnnotationsUtils.getExtensions(*def.extensions).ifEmpty { null }
            AnnotationsUtils.getTags(def.tags, false).ifPresent { tags -> tags.forEach { spec.addTagsItem(it) } }
        }
    }

    /**
     * Processing annotation [SecurityScheme] on class level.
     */
    private fun processSecuritySchemes(clazz: Class<*>, spec: OpenAPI) {
        clazz.getAnnotationsInherited(SecurityScheme::class.java)?.let { schemas ->
            spec.components.securitySchemes = spec.components.securitySchemes ?: mutableMapOf()
            spec.components.securitySchemes.putAll(schemas.map { it.model }.associateBy({ it.first }, { it.second }))
        }
    }

    /**
     * Processing annotation [SecurityRequirement] on class level.
     */
    private fun processSecurityRequirements(clazz: Class<*>): List<SecurityRequirement> =
        when (val security = clazz.getAnnotationsInherited(SecurityRequirement::class.java)?.toTypedArray()) {
            null -> emptyList()
            else -> security.toList()
        }

    /**
     * Processing annotation [Tag] on class level.
     */
    private fun processTags(clazz: Class<*>): List<Tag> =
        when (val tags = clazz.getAnnotationsInherited(Tag::class.java)?.toTypedArray()) {
            null -> emptyList()
            else -> tags.toList()
        }

    /**
     * Processing annotation [Server] on class level.
     */
    private fun processServers(clazz: Class<*>): List<Server> =
        when (val servers = clazz.getAnnotationsInherited(Server::class.java)?.toTypedArray()) {
            null -> emptyList()
            else -> servers.toList()
        }

    /**
     * Processing annotation [SecurityRequirement] on class level.
     */
    private fun processExternalDocumentation(clazz: Class<*>): ExternalDocumentation? =
        clazz.getAnnotationInherited(ExternalDocumentation::class.java)

    /**
     * Processing OpenAPI annotations on class level.
     * @param clazz Class of service
     * @param spec OpenAPI specification. *Note:* to be filled while processing.
     * @return Additional data on class level for processing method level annotations.
     */
    fun process(clazz: Class<*>, spec: OpenAPI): ClassLevelInfo {
        processOpenAPIDefinition(clazz, spec)
        processSecuritySchemes(clazz, spec)
        return ClassLevelInfo(
            processSecurityRequirements(clazz),
            processTags(clazz),
            processServers(clazz),
            processExternalDocumentation(clazz)
        )
    }
}
