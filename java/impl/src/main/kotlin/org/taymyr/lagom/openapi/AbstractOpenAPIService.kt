package org.taymyr.lagom.openapi

import akka.NotUsed
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol
import com.lightbend.lagom.javadsl.api.transport.NotFound
import com.lightbend.lagom.javadsl.server.HeaderServiceCall
import com.typesafe.config.Config
import io.github.config4k.extract
import mu.KLogging
import org.taymyr.lagom.javadsl.api.transport.MessageProtocols.JSON
import org.taymyr.lagom.javadsl.api.transport.MessageProtocols.fromFile
import org.taymyr.lagom.javadsl.api.transport.ResponseHeaders.ok
import play.mvc.Results.ok
import java.util.Optional.ofNullable
import java.util.concurrent.CompletableFuture.completedFuture

abstract class AbstractOpenAPIService(config: Config) : OpenAPIService {

    private val apiSpec: String?

    private val mimeType: MessageProtocol

    init {
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
        this.apiSpec = spec
        this.mimeType = protocol ?: JSON
    }

    override fun openapi(): HeaderServiceCall<NotUsed, String> {
        return HeaderServiceCall { _, _ ->
            completedFuture(
                ok(mimeType, ofNullable(apiSpec).orElseThrow { NotFound("OpenAPI specification not found") })
            )
        }
    }

    companion object : KLogging() {
        private val OPENAPI_PATH = "openapi.file"
    }
}
