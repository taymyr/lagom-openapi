package org.taymyr.lagom.javadsl.openapi

import akka.NotUsed
import com.lightbend.lagom.javadsl.api.Descriptor
import com.lightbend.lagom.javadsl.api.Service
import com.lightbend.lagom.javadsl.api.Service.pathCall
import com.lightbend.lagom.javadsl.api.ServiceCall
import kotlin.reflect.jvm.javaMethod

/**
 * OpenAPI service descriptor.
 */
interface OpenAPIService : Service {

    /**
     * Return OpenAPI specification for current service.
     * @return OpenAPI specification
     */
    fun openapi(): ServiceCall<NotUsed, String>

    @JvmDefault
    fun withOpenAPI(descriptor: Descriptor): Descriptor = descriptor.withCalls(
        pathCall<NotUsed, String>("/_${descriptor.name()}/openapi", OpenAPIService::openapi.javaMethod)
    )
}
