package org.taymyr.lagom.scaladsl.openapi

import com.lightbend.lagom.scaladsl.api.Service
import com.typesafe.config.Config
import play.api.routing.Router
import play.api.routing.sird._
import play.api.mvc.DefaultActionBuilder
import play.api.mvc.Result
import play.api.mvc.Results

class OpenAPIRouter(action: DefaultActionBuilder, config: Config) {

  private def response(spec: String, contentType: String): Result = {
    if (spec == null) Results.NotFound("OpenAPI specification not found")
    else Results.Ok(spec).as(contentType)
  }

  def router(service: Service, path: Option[String] = None): Router = {
    val openapi: OpenAPIContainer = OpenAPIContainer(service, config)
    val route                     = path.getOrElse(s"/_${service.descriptor.name}/openapi")
    Router
      .from {
        case GET(p"$p*") if p.equals(route) =>
          action { request =>
            val isJson = "json".equalsIgnoreCase(request.getQueryString("format").orNull)
            if (isJson) response(openapi.spec.json, "application/json")
            else response(openapi.spec.yaml, "application/x-yaml")
          }
      }
  }
}
