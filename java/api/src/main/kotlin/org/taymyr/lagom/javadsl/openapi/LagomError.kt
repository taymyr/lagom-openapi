package org.taymyr.lagom.javadsl.openapi

import io.swagger.v3.oas.annotations.media.Schema

@Schema(requiredProperties = ["name", "detail"])
class LagomError {
    @Schema(description = "Exception name")
    fun getName() = ""
    @Schema(description = "Exception detail message")
    fun getDetail() = ""
}
