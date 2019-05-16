package org.taymyr.lagom.javadsl.openapi

import akka.NotUsed
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol
import com.lightbend.lagom.javadsl.api.transport.NotFound
import com.lightbend.lagom.javadsl.server.HeaderServiceCall
import com.typesafe.config.Config
import io.github.config4k.extract
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import mu.KLogging
import org.taymyr.lagom.internal.openapi.isAnnotationPresentInherited
import org.taymyr.lagom.javadsl.api.transport.MessageProtocols.JSON
import org.taymyr.lagom.javadsl.api.transport.MessageProtocols.YAML
import org.taymyr.lagom.javadsl.api.transport.MessageProtocols.fromFile
import org.taymyr.lagom.javadsl.api.transport.ResponseHeaders.ok
import java.util.concurrent.CompletableFuture.completedFuture

abstract class AbstractOpenAPIService(config: Config? = null) : OpenAPIService {

    private val spec: OpenAPISpec by lazy {
        val isAnnotated: Boolean = this.javaClass.isAnnotationPresentInherited(OpenAPIDefinition::class.java)
        if (isAnnotated) {
            generateSpecResource()
        } else {
            createSpecResponseFromResource(config)
        }
    }

    private fun generateSpecResource() =
        OpenAPISpec(Yaml.pretty(SpecGenerator().generate(this)), YAML)

    private fun createSpecResponseFromResource(config: Config?): OpenAPISpec {
        var spec: String? = null
        var protocol: MessageProtocol? = null
        val paths = when (val configPath = config?.extract<String?>(SPEC_CONFIG_PATH)) {
            null -> listOf("json", "yaml", "yml").map { "${descriptor().name()}.$it" }
            else -> listOf(configPath)
        }
        for (filename in paths) {
            try {
                val openapiSpec = this.javaClass.getResource("/$filename")
                spec = openapiSpec.readText()
                protocol = fromFile(filename)
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
                ok(spec.mimeType, spec.api ?: throw NotFound("OpenAPI specification not found"))
            )
        }
    }

    companion object : KLogging() {
        private const val SPEC_CONFIG_PATH = "openapi.file"
    }

    private data class OpenAPISpec(val api: String?, var mimeType: MessageProtocol)
}
