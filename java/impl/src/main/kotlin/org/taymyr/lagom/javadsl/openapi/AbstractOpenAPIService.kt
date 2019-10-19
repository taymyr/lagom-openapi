package org.taymyr.lagom.javadsl.openapi

import akka.NotUsed
import akka.japi.Pair
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol
import com.lightbend.lagom.javadsl.api.transport.NotFound
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader.OK
import com.lightbend.lagom.javadsl.server.HeaderServiceCall
import com.typesafe.config.Config
import io.swagger.v3.core.util.Json
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import mu.KLogging
import org.taymyr.lagom.internal.openapi.isAnnotationPresentInherited
import org.taymyr.lagom.internal.openapi.jsonToYaml
import org.taymyr.lagom.internal.openapi.yamlToJson
import java.util.Optional
import java.util.concurrent.CompletableFuture.completedFuture

internal val YAML: MessageProtocol = MessageProtocol.fromContentTypeHeader(Optional.of("application/x-yaml"))
internal val JSON: MessageProtocol = MessageProtocol.fromContentTypeHeader(Optional.of("application/json"))

abstract class AbstractOpenAPIService(config: Config? = null) : OpenAPIService {

    private val spec: OpenAPISpec by lazy {
        val isAnnotated: Boolean = this.javaClass.isAnnotationPresentInherited(OpenAPIDefinition::class.java)
        if (isAnnotated) {
            generateSpecResource()
        } else {
            createSpecResponseFromResource(config)
        }
    }

    private fun fromFile(file: String, default: MessageProtocol): MessageProtocol =
        when (file.substring(file.lastIndexOf(".") + 1)) {
            "json" -> JSON
            "yaml", "yml" -> YAML
            else -> default
        }

    private fun generateSpecResource(): OpenAPISpec {
        val api = SpecGenerator().generate(this)
        return OpenAPISpec(Json.pretty(api), Yaml.pretty(api))
    }

    private fun createSpecResponseFromResource(config: Config?): OpenAPISpec {
        var spec: String? = null
        var protocol: MessageProtocol? = null
        val paths = if (config != null && config.hasPath(SPEC_CONFIG_PATH)) {
            listOf(config.getString(SPEC_CONFIG_PATH))
        } else {
            listOf("json", "yaml", "yml").map { "${descriptor().name()}.$it" }
        }
        for (filename in paths) {
            try {
                val openapiSpec = this.javaClass.getResource("/$filename")
                spec = openapiSpec.readText()
                protocol = fromFile(filename, YAML)
                logger.info { "Load OpenAPI specification from $openapiSpec" }
                break
            } catch (e: Exception) {
            }
        }
        if (spec == null) logger.error { "OpenAPI specification not found in $paths" }
        return when (protocol) {
            JSON -> OpenAPISpec(spec, jsonToYaml(spec))
            YAML -> OpenAPISpec(yamlToJson(spec), spec)
            else -> OpenAPISpec(null, null)
        }
    }

    private fun response(spec: String?, protocol: MessageProtocol) = Pair.create(
        OK.withProtocol(protocol),
        spec ?: throw NotFound("OpenAPI specification not found")
    )

    override fun openapi(format: Optional<String>): HeaderServiceCall<NotUsed, String> =
        HeaderServiceCall { _, _ ->
            val isJson = format.map { it.equals("json", true) }.orElse(false)
            completedFuture(
                if (isJson) response(spec.json, JSON)
                else response(spec.yaml, YAML)
            )
        }

    companion object : KLogging() {
        private const val SPEC_CONFIG_PATH = "openapi.file"
    }

    private data class OpenAPISpec(val json: String?, var yaml: String?)
}
