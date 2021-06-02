package org.taymyr.lagom.javadsl.openapi

import com.lightbend.lagom.javadsl.api.Service
import com.typesafe.config.Config
import play.api.mvc.Handler
import play.api.mvc.RequestHeader
import play.api.routing.Router
import play.api.routing.SimpleRouter
import play.mvc.Http
import play.mvc.Result
import play.mvc.Results.notFound
import play.mvc.Results.ok
import play.routing.RoutingDsl
import scala.PartialFunction
import javax.inject.Provider

class OpenAPIRouter @JvmOverloads constructor(
    routingDsl: Provider<RoutingDsl>,
    serviceProvider: Provider<out Service>,
    configProvider: Provider<Config>,
    path: String? = null
) : SimpleRouter {

    private val service: Service by lazy { serviceProvider.get() }

    private val config: Config by lazy { configProvider.get() }

    private val openapi: OpenAPIContainer by lazy { OpenAPIContainer(service, config) }

    private fun response(spec: String?, contentType: String): Result =
        spec?.let { ok(it).`as`(contentType) } ?: notFound("OpenAPI specification not found")

    private val delegate: Router by lazy {
        routingDsl.get()
            .GET((path ?: "/_${service.descriptor().name()}/openapi"))
            .routingTo { request: Http.Request ->
                val isJson = "json".equals(request.getQueryString("format"), true)
                if (isJson) response(openapi.spec.json, "application/json")
                else response(openapi.spec.yaml, "application/x-yaml")
            }
            .build()
            .asScala()
    }

    override fun routes(): PartialFunction<RequestHeader, Handler> = delegate.routes()
}
