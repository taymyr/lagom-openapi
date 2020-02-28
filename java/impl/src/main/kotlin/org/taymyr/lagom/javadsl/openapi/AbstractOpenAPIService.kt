package org.taymyr.lagom.javadsl.openapi

import akka.NotUsed
import akka.japi.Pair
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol
import com.lightbend.lagom.javadsl.api.transport.NotFound
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader.OK
import com.lightbend.lagom.javadsl.server.HeaderServiceCall
import com.typesafe.config.Config
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import mu.KLogging
import org.taymyr.lagom.internal.openapi.isAnnotationPresentInherited
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

    private fun generateSpecResource() =
        OpenAPISpec(Yaml.pretty(SpecGenerator().generate(this)), YAML)

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
            } catch (e: Exception) { }
        }
        if (spec == null) logger.error { "OpenAPI specification not found in $paths" }
        return OpenAPISpec(spec, protocol ?: JSON)
    }

    override fun openapi(): HeaderServiceCall<NotUsed, String> {
        return HeaderServiceCall { _, _ ->
            completedFuture(
                Pair.create(
                    OK.withProtocol(spec.mimeType),
                    spec.api ?: throw NotFound("OpenAPI specification not found")
                )
            )
        }
    }

    companion object : KLogging() {
        private const val SPEC_CONFIG_PATH = "openapi.file"
    }

    private data class OpenAPISpec(val api: String?, var mimeType: MessageProtocol)
}
