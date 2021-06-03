package org.taymyr.lagom.internal.openapi

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import java.lang.reflect.Method

data class LagomCallInfo(val method: Method, val path: String, val httpMethod: String) {
    fun isHidden(): Boolean {
        val method = this.method
        val operation = method.getAnnotationInherited(Operation::class.java)
        val hidden = method.getAnnotationInherited(Hidden::class.java)
        return operation == null || operation.hidden || hidden != null
    }
}
