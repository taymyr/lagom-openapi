package org.taymyr.lagom.scaladsl.openapi

import io.swagger.v3.oas.annotations.media.Schema

import scala.annotation.meta.field
import scala.beans.BeanProperty

@Schema(requiredProperties = Array("name", "detail"))
case class LagomError(
    @(Schema @field)(description = "Exception name")
    @BeanProperty name: String,
    @(Schema @field)(description = "Exception detail message")
    @BeanProperty detail: String
)
