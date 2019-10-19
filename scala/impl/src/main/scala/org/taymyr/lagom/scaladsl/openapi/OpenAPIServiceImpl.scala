package org.taymyr.lagom.scaladsl.openapi
import com.google.common.base.Charsets
import com.google.common.io.Resources
import com.lightbend.lagom.scaladsl.api.Service
import com.lightbend.lagom.scaladsl.api.transport.MessageProtocol
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.api.transport.ResponseHeader
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import com.typesafe.config.Config
import io.swagger.v3.core.util.Json
import io.swagger.v3.core.util.ReflectionUtils
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.taymyr.lagom.internal.openapi.Utils._

import scala.concurrent.Future
import scala.util.control.Breaks._

trait OpenAPIServiceImpl extends OpenAPIService with Service {

  private final val log: Logger = LoggerFactory.getLogger(classOf[OpenAPIServiceImpl])

  def config: Config

  val YAML: MessageProtocol    = MessageProtocol.fromContentTypeHeader(Some("application/x-yaml"))
  val JSON: MessageProtocol    = MessageProtocol.fromContentTypeHeader(Some("application/json"))
  val SPEC_CONFIG_PATH: String = "openapi.file"

  private def generateSpecResource(): OpenAPISpec = {
    val api = new SpecGenerator().generate(this)
    OpenAPISpec(Json.pretty(api), Yaml.pretty(api))
  }

  private def fromFile(file: String, default: MessageProtocol): MessageProtocol = {
    file.substring(file.lastIndexOf(".") + 1) match {
      case "json"         => JSON
      case "yaml" | "yml" => YAML
      case _              => default
    }
  }

  private def createSpecResponseFromResource(config: Config): OpenAPISpec = {
    var spec: String              = null
    var protocol: MessageProtocol = null
    val paths = if (config.hasPathOrNull(SPEC_CONFIG_PATH)) {
      List(config.getString(SPEC_CONFIG_PATH))
    } else {
      List("json", "yaml", "yml").map { s"${descriptor.name}." + _ }
    }
    breakable {
      for (filename <- paths) {
        var found = false
        try {
          val openapiSpec = this.getClass.getResource(s"/$filename")
          spec = Resources.toString(openapiSpec, Charsets.UTF_8)
          protocol = fromFile(filename, YAML)
          log.info(s"Load OpenAPI specification from $openapiSpec")
          found = true
        } catch {
          case _: Exception =>
        }
        if (found) break
      }
    }
    if (spec == null) log.error(s"OpenAPI specification not found in $paths")
    protocol match {
      case JSON => OpenAPISpec(spec, jsonToYaml(spec))
      case YAML => OpenAPISpec(yamlToJson(spec), spec)
      case _    => OpenAPISpec(null, null)
    }
  }

  private lazy val spec: OpenAPISpec = {
    val isAnnotated: Boolean = ReflectionUtils.getAnnotation(this.getClass, classOf[OpenAPIDefinition]) != null
    if (isAnnotated) {
      generateSpecResource()
    } else {
      createSpecResponseFromResource(config)
    }
  }

  private def response(spec: String, protocol: MessageProtocol) =
    (ResponseHeader.Ok.withProtocol(protocol), spec)

  override def openapi(format: Option[String]) = ServerServiceCall { (_, _) =>
    if (spec == null || spec.json == null || spec.yaml == null) throw NotFound("OpenAPI specification not found")
    Future.successful(
      if (format.exists(f => f.equalsIgnoreCase("json"))) response(spec.json, JSON)
      else response(spec.yaml, YAML)
    )
  }

  private case class OpenAPISpec(json: String, yaml: String)

}
