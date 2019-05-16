package org.taymyr.lagom.internal.openapi

data class LagomServiceInfo(
    val service: Class<*>,
    val calls: List<LagomCallInfo>
)