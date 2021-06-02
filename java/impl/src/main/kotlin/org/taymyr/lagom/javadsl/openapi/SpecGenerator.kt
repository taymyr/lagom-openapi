package org.taymyr.lagom.javadsl.openapi

import akka.NotUsed
import com.google.common.reflect.TypeToken
import com.lightbend.lagom.internal.javadsl.api.MethodRefResolver
import com.lightbend.lagom.internal.javadsl.api.MethodRefServiceCallHolder
import com.lightbend.lagom.javadsl.api.Descriptor.Call
import com.lightbend.lagom.javadsl.api.Descriptor.NamedCallId
import com.lightbend.lagom.javadsl.api.Descriptor.PathCallId
import com.lightbend.lagom.javadsl.api.Descriptor.RestCallId
import com.lightbend.lagom.javadsl.api.Service
import com.lightbend.lagom.javadsl.api.ServiceCall
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem.HttpMethod
import org.taymyr.lagom.internal.openapi.LagomCallInfo
import org.taymyr.lagom.internal.openapi.LagomServiceInfo
import org.taymyr.lagom.internal.openapi.SpecGenerator
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

internal class SpecGenerator(spec: OpenAPI = OpenAPI()) : SpecGenerator(spec) {

    private fun MethodRefServiceCallHolder.toMethod(): Method = when (val methodReference = this.methodReference()) {
        is Method -> methodReference
        else -> {
            try {
                MethodRefResolver.resolveMethodRef(methodReference)
            } catch (t: Throwable) {
                throw IllegalStateException(
                    """Unable to resolve method for service call.
                Ensure that the you have passed a method reference (ie, this::someMethod). Passing anything else,
                for example lambdas, anonymous classes or actual implementation classes, is forbidden in declaring a
                service descriptor.""".trimIndent(), t
                )
            }
        }
    }

    private fun openapiPath(call: Call<*, *>): String = when (val callId = call.callId()) {
        is RestCallId -> openapiPath(callId.pathPattern())
        is PathCallId -> openapiPath(callId.pathPattern())
        is NamedCallId -> "/${callId.name()}"
        else -> throw IllegalArgumentException("${callId::class.java} is not supported")
    }

    private fun hasRequestBody(method: Method): Boolean {
        @Suppress("UNCHECKED_CAST", "UnstableApiUsage")
        val serviceCallType = (TypeToken.of(method.genericReturnType) as TypeToken<ServiceCall<*, *>>)
            .getSupertype(ServiceCall::class.java)
            .type as? ParameterizedType ?: throw IllegalStateException("ServiceCall is not a parameterized type?")
        if (serviceCallType.actualTypeArguments.size != 2) throw IllegalStateException("ServiceCall does not have 2 type arguments?")
        if (method.returnType != ServiceCall::class.java) throw IllegalArgumentException("Service calls must return ServiceCall, subtypes are not allowed")
        return serviceCallType.actualTypeArguments[0] != NotUsed::class.java
    }

    private fun httpMethod(call: Call<*, *>, method: Method): HttpMethod = when (val callId = call.callId()) {
        is RestCallId -> HttpMethod.valueOf(callId.method().name())
        else -> if (hasRequestBody(method)) HttpMethod.POST else HttpMethod.GET
    }

    private fun parseServiceInfo(service: Service): LagomServiceInfo = LagomServiceInfo(
        service = service.javaClass,
        calls = service.descriptor().calls()
            .map { call ->
                when (val callHolder = call.serviceCallHolder()) {
                    is MethodRefServiceCallHolder -> {
                        val method = callHolder.toMethod()
                        LagomCallInfo(method, openapiPath(call), httpMethod(call, method).name)
                    }
                    else -> throw IllegalArgumentException(
                        "Undefined type of ServiceCallHolder, only MethodRefServiceCallHolder is supported at the moment"
                    )
                }
            }
    )

    fun generate(service: Service): OpenAPI = generate(parseServiceInfo(service))
}