package org.taymyr.lagom.internal.openapi

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Paths

open class SpecGenerator(protected val spec: OpenAPI = OpenAPI()) {

    /**
     * Transform Lagom service call path to OpenAPI Specification path.
     */
    protected fun openapiPath(path: String): String =
        path.substringBefore("?").split("/").joinToString("/") { part ->
            if (part.startsWith(":")) "{${part.substring(1)}}" else part
        }

    /**
     * Generate OpenAPI specification by [LagomServiceInfo].
     */
    fun generate(service: LagomServiceInfo): OpenAPI {
        val serviceClass = service.service
        if (serviceClass.isAnnotationPresent(Hidden::class.java)) return spec

        spec.paths = spec.paths ?: Paths()
        spec.components = spec.components ?: Components()

        val classInfo = ServiceClassProcessor.process(serviceClass, spec)
        service.calls.forEach { OperationMethodProcessor.process(it, classInfo, spec) }
        return spec
    }
}
