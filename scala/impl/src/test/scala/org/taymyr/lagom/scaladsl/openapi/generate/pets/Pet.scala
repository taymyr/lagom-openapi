package org.taymyr.lagom.scaladsl.openapi.generate.pets

import io.swagger.v3.oas.annotations.media.Schema

import scala.annotation.meta.field
import scala.beans.BeanProperty

case class Pet(
    @(Schema @field)(required = true)
    @BeanProperty id: Long,
    @(Schema @field)(required = true)
    @BeanProperty name: String,
    @BeanProperty tag: String
)

object Pet {
  import play.api.libs.json._
  implicit val format: Format[Pet] = Json.format[Pet]
}
