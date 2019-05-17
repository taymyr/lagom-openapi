package org.taymyr.lagom.openapi.internal

import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.core.util.AnnotationsUtils
import io.swagger.v3.core.util.ParameterProcessor
import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.Explode
import io.swagger.v3.oas.annotations.enums.ParameterStyle
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.links.Link
import io.swagger.v3.oas.annotations.links.LinkParameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.OAuthFlow
import io.swagger.v3.oas.annotations.security.OAuthFlows
import io.swagger.v3.oas.annotations.security.OAuthScope
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server
import io.swagger.v3.oas.annotations.servers.ServerVariable
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum
import io.swagger.v3.oas.models.security.Scopes
import io.swagger.v3.oas.models.servers.ServerVariables
import io.swagger.v3.oas.models.ExternalDocumentation as ModelExternalDocumentation
import io.swagger.v3.oas.models.Operation as ModelOperation
import io.swagger.v3.oas.models.headers.Header as ModelHeader
import io.swagger.v3.oas.models.links.Link as ModelLink
import io.swagger.v3.oas.models.media.Content as ModelContent
import io.swagger.v3.oas.models.media.Schema as ModelSchema
import io.swagger.v3.oas.models.parameters.Parameter as ModelParameter
import io.swagger.v3.oas.models.parameters.RequestBody as ModelRequestBody
import io.swagger.v3.oas.models.responses.ApiResponse as ModelApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses as ModelApiResponses
import io.swagger.v3.oas.models.security.OAuthFlow as ModelOAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows as ModelOAuthFlows
import io.swagger.v3.oas.models.security.SecurityRequirement as ModelSecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme as ModelSecurityScheme
import io.swagger.v3.oas.models.servers.Server as ModelServer
import io.swagger.v3.oas.models.servers.ServerVariable as ModelServerVariable

internal val modelConverters = ModelConverters.getInstance().apply {
    addConverter(DoneModelConverter())
}

internal val Operation.model: Model<ModelOperation> get() {
    val requestBodyModel = requestBody.model
    val parametersModel = parameters.model
    val responsesModel = responses.model
    val operation = ModelOperation()
        .summary(summary.model)
        .description(description.model)
        .tags(tags.model.ifEmpty { null })
        .requestBody(requestBodyModel.underlying)
        .externalDocs(externalDocs.model)
        .operationId(operationId.model)
        .parameters(parametersModel.map { it.underlying }.ifEmpty { null })
        .responses(responsesModel.underlying)
        .deprecated(deprecated)
        .security(security.model)
        .servers(servers.model)
    val schemas = mutableMapOf<String, ModelSchema<Any>>()
    val links = mutableMapOf<String, ModelLink>()
    schemas.putAll(requestBodyModel.schemas)
    parametersModel.forEach { schemas.putAll(it.schemas) }
    schemas.putAll(responsesModel.schemas)
    links.putAll(responsesModel.links)

    return Model(operation, schemas, links)
}

internal val Array<SecurityRequirement>.model: List<ModelSecurityRequirement>? get() =
    if (isNotEmpty()) listOf(ModelSecurityRequirement().apply { this@model.forEach { putAll(it.model) } })
    else null

internal val SecurityRequirement.model: ModelSecurityRequirement get() = ModelSecurityRequirement().apply {
    if (this@model.name.isNotEmpty()) put(this@model.name, this@model.scopes.model)
}

internal val SecuritySchemeType.model: ModelSecurityScheme.Type? get() =
    ModelSecurityScheme.Type.values().find { it.toString() == this.toString() }

internal val SecuritySchemeIn.model: ModelSecurityScheme.In? get() =
    ModelSecurityScheme.In.values().find { it.toString() == this.toString() }

internal val SecurityScheme.model: Pair<String, ModelSecurityScheme> get() {
    val resScheme = ModelSecurityScheme()
        .type(type.model)
        .description(description).`$ref`(ref.ifEmpty { null })
    when (type.model) {
        ModelSecurityScheme.Type.APIKEY -> resScheme.name(name).`in`(`in`.model)
        ModelSecurityScheme.Type.HTTP -> resScheme.scheme(scheme).bearerFormat(bearerFormat)
        ModelSecurityScheme.Type.OAUTH2 -> resScheme.flows(flows.model)
        ModelSecurityScheme.Type.OPENIDCONNECT -> resScheme.openIdConnectUrl(openIdConnectUrl)
    }
    return Pair(name, resScheme)
}

internal val Array<OAuthScope>?.model: Scopes? get() =
    this?.let { Scopes().apply { this@model.forEach { addString(it.name, it.description) } } }

internal fun OAuthFlow.isEmpty() =
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

internal val OAuthFlow.implicitModel: ModelOAuthFlow? get() = common?.authorizationUrl(authorizationUrl)
internal val OAuthFlow.authorizationCodeModel: ModelOAuthFlow? get() = common?.tokenUrl(tokenUrl)?.authorizationUrl(authorizationUrl)
internal val OAuthFlow.clientCredentialsModel: ModelOAuthFlow? get() = common?.tokenUrl(tokenUrl)
internal val OAuthFlow.passwordModel: ModelOAuthFlow? get() = common?.tokenUrl(tokenUrl)

internal fun OAuthFlows.isEmpty() =
    authorizationCode.isEmpty() &&
    clientCredentials.isEmpty() &&
    implicit.isEmpty() &&
    password.isEmpty() &&
    extensions.isNullOrEmpty()

internal val OAuthFlows?.model: ModelOAuthFlows? get() = when {
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

internal val ExternalDocumentation.model: ModelExternalDocumentation? get() =
    if (url.isNotBlank()) ModelExternalDocumentation().description(description.model).url(url.model)
    else null

internal val Array<Server>.model: List<ModelServer?>? get() =
    if (isNotEmpty()) mapNotNull { it.model }
    else null

internal val Server.model: ModelServer? get() =
    if (url.isNotBlank()) ModelServer().url(url).description(description.model).variables(variables.model)
    else null

internal val ServerVariable.model: ModelServerVariable get() = ModelServerVariable()
    ._enum(allowableValues.model)
    ._default(defaultValue.model)
    .description(description.model)

internal val Array<ServerVariable>.model: ServerVariables? get() =
    if (isNotEmpty()) ServerVariables().apply { this@model.forEach { put(it.name, it.model) } }
    else null

internal val Explode.model: Boolean? get() = when (this) {
    Explode.DEFAULT -> null
    Explode.FALSE -> false
    Explode.TRUE -> true
}

internal val String.model: String? get() = if (isNotBlank()) this else null

internal val Array<String>.model: List<String> get() = filter { it.isNotBlank() }

internal val Array<ApiResponse>.model: Model<ModelApiResponses> get() {
    val model = ModelApiResponses()
    val schemas = mutableMapOf<String, ModelSchema<Any>>()
    val links = mutableMapOf<String, ModelLink>()
    mapNotNull { it.model.underlying }.forEach { model.putAll(it) }
    map { it.model.schemas }.forEach { schemas.putAll(it) }
    map { it.model.links }.forEach { links.putAll(it) }
    return Model(model.ifEmpty { null }, schemas, links)
}

internal val Array<Link>.model: Map<String, ModelLink>? get() =
    if (isNotEmpty()) mutableMapOf<String, ModelLink>().apply { this@model.map { it.model }.forEach { putAll(it) } }
    else null

internal val Link.model: Map<String, ModelLink> get() =
    if (name.isBlank()) emptyMap()
    else mapOf(ref to ModelLink()
        .operationId(operationId.model)
        .operationRef(operationRef.model)
        .description(description.model)
        .requestBody(requestBody.model)
        .server(server.model)
        .`$ref`(ref.model)
        .apply { parameters = this@model.parameters.model })

internal val Array<LinkParameter>.model: Map<String, String>? get() =
    if (isNotEmpty()) filter { it.name.isNotBlank() || it.expression.isNotBlank() }.map { it.name to it.expression }.toMap()
    else null

internal val ApiResponse.model: Model<Map<String, ModelApiResponse>> get() {
    val headersModel = headers.model
    val contentModel = content.model
    val apiResponse = ModelApiResponse()
        .description(description)
        .headers(headersModel.underlying)
        .content(contentModel.underlying)
        .`$ref`(ref.model)
        .apply { links = this@model.links.model }
    return Model(
        if (apiResponse.run { content == null && `$ref` == null && description.isNullOrBlank() }) emptyMap()
        else mapOf(responseCode to apiResponse),
        mutableMapOf<String, ModelSchema<Any>>().apply {
            putAll(headersModel.schemas)
            putAll(contentModel.schemas)
        },
        apiResponse.links ?: emptyMap()
    )
}

internal val Array<Header>.model: Model<Map<String, ModelHeader>> get() {
    val headers = mutableMapOf<String, ModelHeader>()
    val schemas = mutableMapOf<String, ModelSchema<Any>>()
    map { it.model }.forEach {
        it.underlying?.run { headers.putAll(this) }
        schemas.putAll(it.schemas)
    }
    return Model(headers.ifEmpty { null }, schemas)
}

internal val Header.model: Model<Map<String, ModelHeader>> get() {
    val referencedSchemas = mutableMapOf<String, ModelSchema<Any>>()
    val headers: Map<String, ModelHeader> =
        if (name.isBlank()) emptyMap()
        else {
            val schemaModel = schema.model
            referencedSchemas.putAll(schemaModel.schemas)
            val header = ModelHeader()
                .description(description.model)
                .schema(schemaModel.underlying)
                .required(required)
                .deprecated(deprecated)
                .`$ref`(ref.model)
            if (header.schema == null && header.`$ref` == null) emptyMap() else mapOf(name to header)
        }
    return Model(headers, referencedSchemas)
}

internal val Schema.model: Model<ModelSchema<Any>> get() {
    val notSchema =
        if (not.java.isAssignableFrom(Void::class.java)) null
        else modelConverters.resolveAsResolvedSchema(not.java).schema
    val schemas = mutableMapOf<String, ModelSchema<Any>>()
    val schema =
        if (implementation.java.isAssignableFrom(Void::class.java)) ModelSchema<Any>()
        else {
            val resolvedSchema = modelConverters.resolveAsResolvedSchema(implementation.java)
            resolvedSchema.referencedSchemas.forEach { (name, schema) -> schemas[name] = schema }
            resolvedSchema.schema
        }
    schema
        .not(notSchema)
        .name(name.model ?: schema.name)
        .title(title.model ?: schema.title)
        .required(requiredProperties.model.ifEmpty { null } ?: schema.required?.ifEmpty { null })
        .description(description.model ?: schema.description)
        .format(format.model ?: schema.format)
        .`$ref`(ref.model ?: schema.`$ref`)
        .type(type.model ?: schema.type)
    return Model(
        if (schema.type.isNullOrBlank() && schema.`$ref`.isNullOrBlank()) null else schema,
        schemas
    )
}

internal val Parameter.model: Model<ModelParameter> get() {
    val schemaModel = schema.model
    val contentModel = content.model

    val parameter = ModelParameter().`$ref`(ref.ifBlank { null })
    val componentsContainer = Components()
    return Model(
        // TODO support form parameters if needed
        ParameterProcessor.applyAnnotations(
            if (parameter.getIn().isNullOrBlank() || parameter.`$ref`.isNullOrBlank()) parameter else null,
            ParameterProcessor.getParameterType(this),
            listOf(this),
            componentsContainer,
            arrayOfNulls(0),
            arrayOfNulls(0),
            null
        ),
        mutableMapOf<String, ModelSchema<Any>>().apply {
            putAll(schemaModel.schemas)
            putAll(contentModel.schemas)
            putAll(componentsContainer.schemas ?: emptyMap())
        }
    )
}

internal val ParameterStyle.model: StyleEnum? get() = when (this) {
    ParameterStyle.DEFAULT -> null
    else -> StyleEnum.valueOf(this.name)
}

internal val Array<Parameter>.model: List<Model<ModelParameter>> get() = map { it.model }

internal val RequestBody.model: Model<ModelRequestBody> get() {
    val contentModel = content.model
    val requestBody = ModelRequestBody()
        .description(description.model)
        .content(contentModel.underlying)
        .required(required)
        .`$ref`(ref.model)
    return Model(
        if (requestBody.content.isNullOrEmpty() && requestBody.`$ref`.isNullOrBlank()) null else requestBody,
        contentModel.schemas
    )
}

internal val Array<Content>.model: Model<ModelContent> get() {
    val content = ModelContent()
    val schemas = mutableMapOf<String, ModelSchema<Any>>()
    map { it.model }.forEach {
        it.underlying?.run { content.putAll(this) }
        schemas.putAll(it.schemas)
    }
    return Model(content.ifEmpty { null }, schemas)
}

internal val Content.model: Model<ModelContent> get() =
    if (mediaType.isBlank()) Model()
    else {
        val schemas = mutableMapOf<String, ModelSchema<Any>>()
        val schemaModel = schema.model
        val arraySchemaModel = array.schema.model
        schemas.putAll(schemaModel.schemas)
        schemas.putAll(arraySchemaModel.schemas)
        val resultSchema =
            if (schemaModel.underlying == null && arraySchemaModel.underlying != null) ArraySchema().items(arraySchemaModel.underlying)
            else schemaModel.underlying
        if (resultSchema == null) Model()
        else Model(
            ModelContent().addMediaType(mediaType, MediaType().schema(resultSchema)),
            schemas
        )
    }

internal data class Model<M>(
    val underlying: M? = null,
    val schemas: Map<String, ModelSchema<Any>> = emptyMap(),
    val links: Map<String, ModelLink> = emptyMap()
)
