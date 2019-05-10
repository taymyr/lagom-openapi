package io.swagger.v3.lagom

import io.swagger.v3.core.util.AnnotationsUtils
import io.swagger.v3.oas.annotations.security.OAuthScope
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.Scopes
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.apache.commons.lang3.StringUtils
import java.util.Arrays
import java.util.Optional
import kotlin.collections.ArrayList

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
class SecurityParser {

    fun getSecurityRequirements(
        securityRequirementsApi: Array<io.swagger.v3.oas.annotations.security.SecurityRequirement>?
    ): Optional<List<SecurityRequirement>> {
        if (securityRequirementsApi == null || securityRequirementsApi.isEmpty()) {
            return Optional.empty()
        }
        val securityRequirements = ArrayList<SecurityRequirement>()
        for (securityRequirementApi in securityRequirementsApi) {
            if (StringUtils.isBlank(securityRequirementApi.name)) {
                continue
            }
            val securityRequirement = SecurityRequirement()
            if (securityRequirementApi.scopes.isNotEmpty()) {
                securityRequirement.addList(securityRequirementApi.name, Arrays.asList(*securityRequirementApi.scopes))
            } else {
                securityRequirement.addList(securityRequirementApi.name)
            }
            securityRequirements.add(securityRequirement)
        }
        return if (securityRequirements.isEmpty()) {
            Optional.empty()
        } else Optional.of(securityRequirements)
    }

    fun getSecurityScheme(securityScheme: io.swagger.v3.oas.annotations.security.SecurityScheme?): Optional<SecuritySchemePair> {
        if (securityScheme == null) {
            return Optional.empty<SecuritySchemePair>()
        }
        var key: String = ""
        val securitySchemeObject = SecurityScheme()

        if (StringUtils.isNotBlank(securityScheme.`in`.toString())) {
            securitySchemeObject.setIn(getIn(securityScheme.`in`.toString()))
        }
        if (StringUtils.isNotBlank(securityScheme.type.toString())) {
            securitySchemeObject.type = getType(securityScheme.type.toString())
        }

        if (StringUtils.isNotBlank(securityScheme.openIdConnectUrl)) {
            securitySchemeObject.openIdConnectUrl = securityScheme.openIdConnectUrl
        }
        if (StringUtils.isNotBlank(securityScheme.scheme)) {
            securitySchemeObject.scheme = securityScheme.scheme
        }

        if (StringUtils.isNotBlank(securityScheme.bearerFormat)) {
            securitySchemeObject.bearerFormat = securityScheme.bearerFormat
        }
        if (StringUtils.isNotBlank(securityScheme.description)) {
            securitySchemeObject.description = securityScheme.description
        }
        if (StringUtils.isNotBlank(securityScheme.paramName)) {
            securitySchemeObject.name = securityScheme.paramName
        }
        if (StringUtils.isNotBlank(securityScheme.ref)) {
            securitySchemeObject.`$ref` = securityScheme.ref
        }
        if (StringUtils.isNotBlank(securityScheme.name)) {
            key = securityScheme.name
        }

        if (securityScheme.extensions.isNotEmpty()) {
            val extensions = AnnotationsUtils.getExtensions(*securityScheme.extensions)
            if (extensions != null) {
                for (ext in extensions.keys) {
                    securitySchemeObject.addExtension(ext, extensions[ext])
                }
            }
        }

        getOAuthFlows(securityScheme.flows).ifPresent { securitySchemeObject.flows = it }

        return Optional.of<SecuritySchemePair>(SecuritySchemePair(key, securitySchemeObject))
    }

    private fun getOAuthFlows(oAuthFlows: io.swagger.v3.oas.annotations.security.OAuthFlows): Optional<OAuthFlows> {
        if (isEmpty(oAuthFlows)) {
            return Optional.empty()
        }
        val oAuthFlowsObject = OAuthFlows()
        if (oAuthFlows.extensions.isNotEmpty()) {
            val extensions = AnnotationsUtils.getExtensions(*oAuthFlows.extensions)
            if (extensions != null) {
                for (ext in extensions.keys) {
                    oAuthFlowsObject.addExtension(ext, extensions[ext])
                }
            }
        }

        getOAuthFlow(oAuthFlows.authorizationCode).ifPresent { oAuthFlowsObject.authorizationCode = it }
        getOAuthFlow(oAuthFlows.clientCredentials).ifPresent { oAuthFlowsObject.clientCredentials = it }
        getOAuthFlow(oAuthFlows.implicit).ifPresent { oAuthFlowsObject.implicit = it }
        getOAuthFlow(oAuthFlows.password).ifPresent { oAuthFlowsObject.password = it }
        return Optional.of(oAuthFlowsObject)
    }

    private fun getOAuthFlow(oAuthFlow: io.swagger.v3.oas.annotations.security.OAuthFlow): Optional<OAuthFlow> {
        if (isEmpty(oAuthFlow)) {
            return Optional.empty()
        }
        val oAuthFlowObject = OAuthFlow()
        if (StringUtils.isNotBlank(oAuthFlow.authorizationUrl)) {
            oAuthFlowObject.authorizationUrl = oAuthFlow.authorizationUrl
        }
        if (StringUtils.isNotBlank(oAuthFlow.refreshUrl)) {
            oAuthFlowObject.refreshUrl = oAuthFlow.refreshUrl
        }
        if (StringUtils.isNotBlank(oAuthFlow.tokenUrl)) {
            oAuthFlowObject.tokenUrl = oAuthFlow.tokenUrl
        }
        if (oAuthFlow.extensions.isNotEmpty()) {
            val extensions = AnnotationsUtils.getExtensions(*oAuthFlow.extensions)
            if (extensions != null) {
                for (ext in extensions.keys) {
                    oAuthFlowObject.addExtension(ext, extensions[ext])
                }
            }
        }

        getScopes(oAuthFlow.scopes).ifPresent { oAuthFlowObject.scopes = it }
        return Optional.of(oAuthFlowObject)
    }

    fun getScopes(scopes: Array<OAuthScope>): Optional<Scopes> {
        if (isEmpty(scopes)) {
            return Optional.empty()
        }
        val scopesObject = Scopes()

        for (scope in scopes) {
            scopesObject.addString(scope.name, scope.description)
        }
        return Optional.of(scopesObject)
    }

    private fun getIn(value: String): SecurityScheme.In {
        return Arrays.stream<SecurityScheme.In>(SecurityScheme.In.values())
            .filter { i -> i.toString() == value }.findFirst().orElse(null)
    }

    private fun getType(value: String): SecurityScheme.Type {
        return Arrays.stream<SecurityScheme.Type>(SecurityScheme.Type.values())
            .filter { i -> i.toString() == value }.findFirst().orElse(null)
    }

    private fun isEmpty(oAuthFlows: io.swagger.v3.oas.annotations.security.OAuthFlows?): Boolean {
        if (oAuthFlows == null) {
            return true
        }
        if (!isEmpty(oAuthFlows.implicit)) {
            return false
        }
        if (!isEmpty(oAuthFlows.authorizationCode)) {
            return false
        }
        if (!isEmpty(oAuthFlows.clientCredentials)) {
            return false
        }
        if (!isEmpty(oAuthFlows.password)) {
            return false
        }
        return oAuthFlows.extensions.isEmpty()
    }

    private fun isEmpty(oAuthFlow: io.swagger.v3.oas.annotations.security.OAuthFlow?): Boolean {
        if (oAuthFlow == null) {
            return true
        }
        if (!StringUtils.isBlank(oAuthFlow.authorizationUrl)) {
            return false
        }
        if (!StringUtils.isBlank(oAuthFlow.refreshUrl)) {
            return false
        }
        if (!StringUtils.isBlank(oAuthFlow.tokenUrl)) {
            return false
        }
        if (!isEmpty(oAuthFlow.scopes)) {
            return false
        }
        return oAuthFlow.extensions.isEmpty()
    }

    private fun isEmpty(scopes: Array<OAuthScope>?): Boolean {
        return !(scopes != null && scopes.isNotEmpty())
    }

    data class SecuritySchemePair(val key: String, val securityScheme: SecurityScheme)
}