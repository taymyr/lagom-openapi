package org.taymyr.lagom.internal.openapi

import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.servers.Server
import io.swagger.v3.oas.annotations.tags.Tag

data class ClassLevelInfo(
    val securityRequirements: List<SecurityRequirement>,
    val tags: List<Tag>,
    val servers: List<Server>,
    val externalDocumentation: ExternalDocumentation?
)