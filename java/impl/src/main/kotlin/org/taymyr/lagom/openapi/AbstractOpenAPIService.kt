package org.taymyr.lagom.openapi

import akka.NotUsed
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol
import com.lightbend.lagom.javadsl.api.transport.NotFound
import com.lightbend.lagom.javadsl.server.HeaderServiceCall
import com.typesafe.config.Config
import io.github.config4k.extract
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.lagom.ServiceReader
import io.swagger.v3.lagom.isAnnotationPresentInherited
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.models.OpenAPI
import mu.KLogging
import org.taymyr.lagom.javadsl.api.transport.MessageProtocols.JSON
import org.taymyr.lagom.javadsl.api.transport.MessageProtocols.YAML
import org.taymyr.lagom.javadsl.api.transport.MessageProtocols.fromFile
import org.taymyr.lagom.javadsl.api.transport.ResponseHeaders.ok
import java.util.Optional.ofNullable
import java.util.concurrent.CompletableFuture.completedFuture

abstract class AbstractOpenAPIService(config: Config) : OpenAPIService {

    companion object : KLogging() {
        private val OPENAPI_PATH = "openapi.file"
    }

    private val spec: SpecResponse

    private var isAnnotated: Boolean = this.javaClass.isAnnotationPresentInherited(OpenAPIDefinition::class.java)

    init {
        if (isAnnotated) {
            this.spec = generateSpecResource()
        } else {
            this.spec = createSpecResponseFromResource(config)
        }
    }

    private fun generateSpecResource(): SpecResponse {
        return SpecResponse(toYaml(ServiceReader().read(this)), YAML)
    }

    private fun toYaml(swagger: OpenAPI) = Yaml.pretty(swagger)!!

    private fun createSpecResponseFromResource(config: Config): SpecResponse {
        var spec: String? = null
        var protocol: MessageProtocol? = null
        val configPath = config.extract<String?>(OPENAPI_PATH)
        val paths = when (configPath) {
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
            } catch (e: Exception) {
                // do nothing
            }
        }
        if (spec == null) logger.error { "OpenAPI specification not found in $paths" }
        return SpecResponse(spec, protocol ?: JSON)
    }

    override fun openapi(): HeaderServiceCall<NotUsed, String> {
        return HeaderServiceCall { _, _ ->
            completedFuture(
                ok(spec.mimeType, ofNullable(spec.api).orElseThrow { NotFound("OpenAPI specification not found") })
            )
        }
    }

    data class SpecResponse(val api: String?, var mimeType: MessageProtocol)
}
