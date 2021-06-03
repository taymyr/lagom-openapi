@file:JvmName("AnnotationExtensions")

package org.taymyr.lagom.internal.openapi

import io.swagger.v3.core.util.AnnotationsUtils
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.OAuthFlow
import io.swagger.v3.oas.annotations.security.OAuthFlows
import io.swagger.v3.oas.annotations.security.OAuthScope
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.security.Scopes
import io.swagger.v3.oas.models.security.SecurityScheme.In
import io.swagger.v3.oas.models.security.SecurityScheme.Type
import io.swagger.v3.oas.models.security.OAuthFlow as ModelOAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows as ModelOAuthFlows
import io.swagger.v3.oas.models.security.SecurityRequirement as ModelSecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme as ModelSecurityScheme

internal val Array<SecurityRequirement>?.model: List<ModelSecurityRequirement>? get() =
    if (isNullOrEmpty()) null
    else listOf(ModelSecurityRequirement().apply { this@model?.forEach { putAll(it.model) } })

internal val SecurityRequirement.model: ModelSecurityRequirement get() = ModelSecurityRequirement().apply {
    if (this@model.name.isNotEmpty()) put(this@model.name, this@model.scopes.filter { it.isNotBlank() })
}

internal val SecurityScheme.model: Pair<String, ModelSecurityScheme> get() {
    val resScheme = ModelSecurityScheme()
        .type(type.model)
        .description(description).`$ref`(ref.ifEmpty { null })
    when (type.model) {
        Type.APIKEY -> resScheme.name(name).`in`(`in`.model)
        Type.HTTP -> resScheme.scheme(scheme).bearerFormat(bearerFormat)
        Type.OAUTH2 -> resScheme.flows(flows.model)
        Type.OPENIDCONNECT -> resScheme.openIdConnectUrl(openIdConnectUrl)
    }
    return Pair(name, resScheme)
}

private val SecuritySchemeType.model: Type? get() =
    Type.values().find { it.toString() == this.toString() }

private val SecuritySchemeIn.model: In? get() =
    In.values().find { it.toString() == this.toString() }

private val Array<OAuthScope>?.model: Scopes? get() =
    this?.let { Scopes().apply { this@model.forEach { addString(it.name, it.description) } } }

private fun OAuthFlow.isEmpty() =
    authorizationUrl.isBlank() && refreshUrl.isBlank() && tokenUrl.isBlank() && scopes.isNullOrEmpty() && extensions.isNullOrEmpty()

private val OAuthFlow?.common: ModelOAuthFlow? get() = when {
    this == null || this.isEmpty() -> null
    else -> {
        ModelOAuthFlow()
            .refreshUrl(refreshUrl)
            .scopes(scopes.model)
            .apply {
                AnnotationsUtils.getExtensions(*this@common.extensions).forEach { addExtension(it.key, it.value) }
            }
    }
}

private val OAuthFlow.implicitModel: ModelOAuthFlow? get() = common?.authorizationUrl(authorizationUrl)
private val OAuthFlow.authorizationCodeModel: ModelOAuthFlow? get() = common?.tokenUrl(tokenUrl)?.authorizationUrl(authorizationUrl)
private val OAuthFlow.clientCredentialsModel: ModelOAuthFlow? get() = common?.tokenUrl(tokenUrl)
private val OAuthFlow.passwordModel: ModelOAuthFlow? get() = common?.tokenUrl(tokenUrl)

private fun OAuthFlows.isEmpty() =
    authorizationCode.isEmpty() &&
        clientCredentials.isEmpty() &&
        implicit.isEmpty() &&
        password.isEmpty() &&
        extensions.isNullOrEmpty()

private val OAuthFlows?.model: ModelOAuthFlows? get() = when {
    this == null || this.isEmpty() -> null
    else -> ModelOAuthFlows()
        .authorizationCode(authorizationCode.authorizationCodeModel)
        .clientCredentials(clientCredentials.clientCredentialsModel)
        .implicit(implicit.implicitModel)
        .password(password.passwordModel)
        .apply {
            AnnotationsUtils.getExtensions(*this@model.extensions).forEach { addExtension(it.key, it.value) }
        }
}
