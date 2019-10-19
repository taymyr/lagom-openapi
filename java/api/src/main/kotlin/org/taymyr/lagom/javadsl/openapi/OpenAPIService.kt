package org.taymyr.lagom.javadsl.openapi

import akka.NotUsed
import com.lightbend.lagom.javadsl.api.Descriptor
import com.lightbend.lagom.javadsl.api.Service
import com.lightbend.lagom.javadsl.api.Service.pathCall
import com.lightbend.lagom.javadsl.api.ServiceCall
import java.util.Optional
import kotlin.reflect.jvm.javaMethod

/**
 * OpenAPI service descriptor.
 */
interface OpenAPIService : Service {

    /**
     * Return OpenAPI specification for current service.
     * @param format Format of OpenAPI specification. Can be `json` or `yaml`.
     * @return OpenAPI specification
     */
    fun openapi(format: Optional<String>): ServiceCall<NotUsed, String>

    @JvmDefault
    fun withOpenAPI(descriptor: Descriptor): Descriptor = descriptor.withCalls(
        pathCall<NotUsed, String>("/_${descriptor.name()}/openapi", OpenAPIService::openapi.javaMethod)
    )
}
