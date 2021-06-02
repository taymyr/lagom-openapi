package org.taymyr.lagom.scaladsl.openapi

import com.lightbend.lagom.scaladsl.api.Service
import com.lightbend.lagom.scaladsl.api.transport.MessageProtocol
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.api.transport.ResponseHeader
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import com.typesafe.config.Config
import org.taymyr.lagom.scaladsl.openapi.OpenAPIContainer.JSON
import org.taymyr.lagom.scaladsl.openapi.OpenAPIContainer.YAML

import scala.concurrent.Future

@deprecated("Use `OpenAPIRouter` instead", "1.3.0")
trait OpenAPIServiceImpl extends OpenAPIService with Service {

  def config: Config

  private lazy val openapi: OpenAPIContainer = OpenAPIContainer(this, config)

  private def response(spec: String, protocol: MessageProtocol) =
    (ResponseHeader.Ok.withProtocol(protocol), spec)

  override def openapi(format: Option[String]) = ServerServiceCall { (_, _) =>
    val spec = openapi.spec
    if (spec == null || spec.json == null || spec.yaml == null) throw NotFound("OpenAPI specification not found")
    Future.successful(
      if (format.exists(f => f.equalsIgnoreCase("json"))) response(spec.json, JSON)
      else response(spec.yaml, YAML)
    )
  }

}
