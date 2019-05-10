package io.swagger.v3.lagom

import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.enums.Explode
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.enums.ParameterStyle
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.links.Link
import io.swagger.v3.oas.annotations.links.LinkParameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.servers.Server
import io.swagger.v3.oas.annotations.servers.ServerVariable
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.servers.ServerVariables

class Utils {
    companion object {
        val modelConverter = ModelConverters.getInstance()!!
        init {
            modelConverter.addConverter(DoneModelConverter())
        }
    }
}

fun io.swagger.v3.oas.annotations.Operation.toModel(): Model<io.swagger.v3.oas.models.Operation> {
    val requestBodyModel = this.requestBody.toModel()
    val parameterModels = this.parameters.toModel()
    val apiResponsesModel = this.responses.toModel()
    val operation = io.swagger.v3.oas.models.Operation()
        .summary(this.summary.toModel())
        .description(this.description.toModel())
        .tags(tags.toModel())
        .requestBody(requestBodyModel.model)
        .externalDocs(this.externalDocs.toModel())
        .operationId(this.operationId.toModel())
        .parameters(if (parameterModels.isEmpty()) null else parameterModels.map { it.model })
        .responses(apiResponsesModel.model)
        .deprecated(this.deprecated)
        .security(this.security.toModel())
        .servers(this.servers.toModel())
    val referencedSchemas = mutableMapOf<String, io.swagger.v3.oas.models.media.Schema<Any>>()
    referencedSchemas.putAll(requestBodyModel.referencedSchemas)
    parameterModels.forEach { referencedSchemas.putAll(it.referencedSchemas) }
    referencedSchemas.putAll(apiResponsesModel.referencedSchemas)
    return Model(operation, referencedSchemas)
}

fun Array<SecurityRequirement>.toModel(): List<io.swagger.v3.oas.models.security.SecurityRequirement>? {
    val sr = io.swagger.v3.oas.models.security.SecurityRequirement()
    this.forEach { t -> sr.putAll(t.toModel() as Map<String, List<String>>) }
    return if (sr.isEmpty()) null else listOf(sr)
}

fun SecurityRequirement.toModel(): io.swagger.v3.oas.models.security.SecurityRequirement? {
    val sr = io.swagger.v3.oas.models.security.SecurityRequirement()
    if (!this.name.isEmpty()) {
        sr[this.name] = this.scopes.toModel()
    }
    return sr
}

fun ExternalDocumentation.toModel(): io.swagger.v3.oas.models.ExternalDocumentation? {
    val doc = io.swagger.v3.oas.models.ExternalDocumentation()
        .description(description)
        .url(url.toModel())
    return if (doc.description.isBlank()) null else doc
}

fun Array<Server>.toModel(): List<io.swagger.v3.oas.models.servers.Server?>? {
    val list = this.map { t -> t.toModel() }.filter { t -> t != null }
    return if (list.isEmpty()) null else list
}

fun Server.toModel(): io.swagger.v3.oas.models.servers.Server? {
    val server = io.swagger.v3.oas.models.servers.Server()
        .url(url)
        .description(description.toModel())
        .variables(variables.toModel())
    return if (server.url.isNullOrBlank()) null else server
}

fun ServerVariable.toModel() = io.swagger.v3.oas.models.servers.ServerVariable()
    ._enum(allowableValues.toModel())
    ._default(defaultValue.toModel())
    .description(description.toModel())!!

fun Array<ServerVariable>.toModel(): ServerVariables? {
    val serverVariables = ServerVariables()
    this.filter { t -> t.name.isBlank() }
        .forEach { t -> serverVariables[t.name] = t.toModel() }
    return if (serverVariables.isEmpty()) null else serverVariables
}

fun Explode.toModel(): Boolean? {
    return when (this) {
        Explode.DEFAULT -> null
        Explode.FALSE -> false
        Explode.TRUE -> true
    }
}

fun String.toModel(): String? {
    return if (this.isBlank()) return null else this
}

fun Array<String>.toModel(): List<String>? {
    val list = this.filter { t -> !t.isBlank() }
    return if (list.isEmpty()) null else list
}

fun Array<io.swagger.v3.oas.annotations.responses.ApiResponse>.toModel(): Model<io.swagger.v3.oas.models.responses.ApiResponses> {
    val ar = io.swagger.v3.oas.models.responses.ApiResponses()
    val referencedSchemas = mutableMapOf<String, io.swagger.v3.oas.models.media.Schema<Any>>()
    this.forEach {
        val model = it.toModel()
        model.model?.let { ar.putAll(model.model) }
        referencedSchemas.putAll(model.referencedSchemas)
    }
    return Model(
        if (ar.isEmpty()) null else ar,
        referencedSchemas
    )
}

fun Array<Link>.toModel(): Map<String, io.swagger.v3.oas.models.links.Link>? {
    val map = mutableMapOf<String, io.swagger.v3.oas.models.links.Link>()
    this.forEach { t -> map.putAll(t.toModel()) }
    return if (map.isEmpty()) null else map
}

fun Link.toModel(): Map<String, io.swagger.v3.oas.models.links.Link> {
    if (this.name.isBlank()) return emptyMap()
    val link = io.swagger.v3.oas.models.links.Link()
        .operationId(this.operationId.toModel())
        .operationRef(this.operationRef.toModel())
        .description(this.description.toModel())
        .requestBody(this.requestBody.toModel())
        .server(this.server.toModel())
        .`$ref`(this.ref.toModel())
    link.parameters = this.parameters.toModel()
    return mapOf(this.name to link)
}

fun Array<LinkParameter>.toModel(): Map<String, String> {
    return this.filter { t -> !(t.name.isBlank() || t.expression.isBlank()) }
        .map { t -> t.name to t.expression }.toMap()
}

fun io.swagger.v3.oas.annotations.responses.ApiResponse.toModel(): Model<Map<String, io.swagger.v3.oas.models.responses.ApiResponse>> {
    val referencedSchemas = mutableMapOf<String, io.swagger.v3.oas.models.media.Schema<Any>>()
    val headersModel = this.headers.toModel()
    val contentModel = this.content.toModel()
    val response = io.swagger.v3.oas.models.responses.ApiResponse()
        .description(this.description)
        .headers(headersModel.model)
        .content(contentModel.model)
        .`$ref`(this.ref.toModel())
    referencedSchemas.putAll(headersModel.referencedSchemas)
    referencedSchemas.putAll(contentModel.referencedSchemas)
    response.links = this.links.toModel()
    return Model(
        if (response.content == null && response.`$ref` == null && response.description.isNullOrBlank())
            emptyMap()
        else mapOf(this.responseCode to response),
        referencedSchemas
    )
}

fun Array<Header>.toModel(): Model<Map<String, io.swagger.v3.oas.models.headers.Header>> {
    val map = mutableMapOf<String, io.swagger.v3.oas.models.headers.Header>()
    val referencedSchemas = mutableMapOf<String, io.swagger.v3.oas.models.media.Schema<Any>>()
    this.map {
        val headerModel = it.toModel()
        headerModel.model?.let { map.putAll(headerModel.model) }
        referencedSchemas.putAll(headerModel.referencedSchemas)
    }
    return Model(
        if (map.isEmpty()) null else map,
        referencedSchemas
    )
}

fun Header.toModel(): Model<Map<String, io.swagger.v3.oas.models.headers.Header>> {
    val referencedSchemas = mutableMapOf<String, Schema<Any>>()
    val headers: Map<String, io.swagger.v3.oas.models.headers.Header> = if (this.name.isBlank()) {
        emptyMap()
    } else {
        val schemaModel = this.schema.toModel()
        val header = io.swagger.v3.oas.models.headers.Header()
            .description(this.description.toModel())
            .schema(schemaModel.model)
            .required(this.required)
            .deprecated(this.deprecated)
            .`$ref`(this.ref.toModel())
        referencedSchemas.putAll(schemaModel.referencedSchemas)
        if (header.schema == null && header.`$ref` == null) emptyMap() else mapOf(this.name to header)
    }
    return Model(headers, referencedSchemas)
}

fun io.swagger.v3.oas.annotations.media.Schema.toModel(): Model<io.swagger.v3.oas.models.media.Schema<Any>> {
    val notSchema = if (this.not.java.isAssignableFrom(Void::class.java)) null else
        Utils.modelConverter.resolveAsResolvedSchema(this.not.java).schema
    val referencedSchemas = mutableMapOf<String, Schema<Any>>()
    val schema = if (this.implementation.java.isAssignableFrom(Void::class.java))
        io.swagger.v3.oas.models.media.Schema<Any>()
    else {
        val resolvedSchema = Utils.modelConverter.resolveAsResolvedSchema(this.implementation.java)
        resolvedSchema.referencedSchemas.forEach { (name, schema) -> referencedSchemas[name] = schema }
        resolvedSchema.schema
    }
    schema
        .not(notSchema)
        .name(this.name.toModel() ?: schema.name)
        .title(this.title.toModel() ?: schema.title)
        .required(this.requiredProperties.toModel() ?: schema.required)
        .description(this.description.toModel() ?: schema.description)
        .format(this.format.toModel() ?: schema.format)
        .`$ref`(this.ref.toModel() ?: schema.`$ref`)
        .type(this.type.toModel() ?: schema.type)
    return Model(
        if (schema.type.isNullOrBlank() && schema.`$ref`.isNullOrBlank()) null else schema,
        referencedSchemas
    )
}

fun io.swagger.v3.oas.annotations.Parameter.toModel(): Model<io.swagger.v3.oas.models.parameters.Parameter> {
    val schemaModel = this.schema.toModel()
    val contentModel = this.content.toModel()
    val referencedSchemas = mutableMapOf<String, io.swagger.v3.oas.models.media.Schema<Any>>()
    val parameter = io.swagger.v3.oas.models.parameters.Parameter()
        .`in`(this.`in`.toString())
        .name(this.name.toModel())
        .description(this.description.toModel())
        .required(this.required)
        .deprecated(this.deprecated)
        .allowEmptyValue(this.allowEmptyValue)
        .style(this.style.toModel())
        .explode(this.explode.toModel())
        .allowReserved(this.allowReserved)
        .schema(schemaModel.model)
        .content(contentModel.model)
    referencedSchemas.putAll(schemaModel.referencedSchemas)
    referencedSchemas.putAll(contentModel.referencedSchemas)
    if (this.`in` == ParameterIn.PATH) {
        parameter.required = true
    }
    return Model(
        if (parameter.`in`.isNullOrBlank() || parameter.name.isNullOrBlank()) null else parameter,
        referencedSchemas
    )
}

fun ParameterStyle.toModel(): io.swagger.v3.oas.models.parameters.Parameter.StyleEnum? {
    return when (this) {
        ParameterStyle.DEFAULT -> null
        else -> io.swagger.v3.oas.models.parameters.Parameter.StyleEnum.valueOf(this.toString())
    }
}

fun Array<io.swagger.v3.oas.annotations.Parameter>.toModel(): List<Model<io.swagger.v3.oas.models.parameters.Parameter>> {
    val list = this.map { t -> t.toModel() }
    return if (list.isEmpty()) emptyList() else list
}

fun RequestBody.toModel(): Model<io.swagger.v3.oas.models.parameters.RequestBody> {
    val contentModel = this.content.toModel()
    val rb = io.swagger.v3.oas.models.parameters.RequestBody()
        .description(this.description.toModel())
        .content(contentModel.model)
        .required(this.required)
        .`$ref`(this.ref.toModel())
    return Model<io.swagger.v3.oas.models.parameters.RequestBody>(
        if ((rb.content == null || rb.content.isEmpty()) && rb.`$ref`.isNullOrBlank()) null else rb,
        contentModel.referencedSchemas
    )
}

fun Array<Content>.toModel(): Model<io.swagger.v3.oas.models.media.Content> {
    val content = io.swagger.v3.oas.models.media.Content()
    val referencedSchemas = mutableMapOf<String, io.swagger.v3.oas.models.media.Schema<Any>>()
    this.map {
        it.toModel()
    }.forEach {
        it.model?.forEach { k, v -> content[k] = v }
        referencedSchemas.putAll(it.referencedSchemas)
    }
    return Model(
        if (content.isEmpty()) null else content,
        referencedSchemas
    )
}

fun Content.toModel(): Model<io.swagger.v3.oas.models.media.Content> {
    if (this.mediaType.isBlank()) return Model(null, emptyMap())
    val referencedSchemas = mutableMapOf<String, Schema<Any>>()
    val schema = this.schema.toModel()
    val arraySchema = this.array.schema.toModel()
    referencedSchemas.putAll(schema.referencedSchemas)
    referencedSchemas.putAll(arraySchema.referencedSchemas)
    val resultSchema = if (schema.model == null && arraySchema.model != null) {
        ArraySchema().items(arraySchema.model)
    } else schema.model ?: return Model(null, emptyMap())
    return Model(
        io.swagger.v3.oas.models.media.Content().addMediaType(mediaType, MediaType().schema(resultSchema)),
        referencedSchemas
    )
}

data class Model<M>(val model: M?, val referencedSchemas: Map<String, io.swagger.v3.oas.models.media.Schema<Any>> = emptyMap())
