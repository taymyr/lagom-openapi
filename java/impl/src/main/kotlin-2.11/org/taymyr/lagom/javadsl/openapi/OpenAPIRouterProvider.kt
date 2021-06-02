package org.taymyr.lagom.javadsl.openapi

import com.lightbend.lagom.javadsl.api.Service
import com.typesafe.config.Config
import play.api.routing.Router
import play.mvc.Http
import play.mvc.Result
import play.mvc.Results.notFound
import play.mvc.Results.ok
import play.routing.RoutingDsl
import javax.inject.Provider

@Deprecated("Migrate from Scala 2.11 and use `OpenAPIRouter` instead")
class OpenAPIRouterProvider @JvmOverloads constructor(
    routingDsl: Provider<RoutingDsl>,
    serviceProvider: Provider<out Service>,
    configProvider: Provider<Config>,
    path: String? = null
) {

    private val service: Service by lazy { serviceProvider.get() }

    private val config: Config by lazy { configProvider.get() }

    private val openapi: OpenAPIContainer by lazy { OpenAPIContainer(service, config) }

    private fun response(spec: String?, contentType: String): Result =
        spec?.let { ok(it).`as`(contentType) } ?: notFound("OpenAPI specification not found")

    val router: Router by lazy {
        routingDsl.get()
            .GET((path ?: "/_${service.descriptor().name()}/openapi"))
            .routeTo { request: Http.Request ->
                val isJson = "json".equals(request.getQueryString("format"), true)
                if (isJson) response(openapi.spec.json, "application/json")
                else response(openapi.spec.yaml, "application/x-yaml")
            }
            .build()
            .asScala()
    }
}
