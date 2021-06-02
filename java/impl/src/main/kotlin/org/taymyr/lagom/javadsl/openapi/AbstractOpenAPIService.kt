package org.taymyr.lagom.javadsl.openapi

import akka.NotUsed
import akka.japi.Pair
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol
import com.lightbend.lagom.javadsl.api.transport.NotFound
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader.OK
import com.lightbend.lagom.javadsl.server.HeaderServiceCall
import com.typesafe.config.Config
import java.util.Optional
import java.util.concurrent.CompletableFuture.completedFuture

internal val YAML: MessageProtocol = MessageProtocol.fromContentTypeHeader(Optional.of("application/x-yaml"))
internal val JSON: MessageProtocol = MessageProtocol.fromContentTypeHeader(Optional.of("application/json"))

@Deprecated("Use `OpenAPIRouter` instead")
abstract class AbstractOpenAPIService(config: Config? = null) : OpenAPIService {

    private val openapi: OpenAPIContainer by lazy {
        OpenAPIContainer(this, config)
    }

    private fun response(spec: String?, protocol: MessageProtocol) = Pair.create(
        OK.withProtocol(protocol),
        spec ?: throw NotFound("OpenAPI specification not found")
    )

    override fun openapi(format: Optional<String>): HeaderServiceCall<NotUsed, String> =
        HeaderServiceCall { _, _ ->
            val isJson = format.map { it.equals("json", true) }.orElse(false)
            completedFuture(
                if (isJson) response(openapi.spec.json, JSON)
                else response(openapi.spec.yaml, YAML)
            )
        }
}
