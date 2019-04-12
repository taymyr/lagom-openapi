package org.taymyr.lagom.openapi

import com.google.common.base.Joiner
import com.lightbend.lagom.internal.javadsl.api.MethodRefServiceCallHolder
import com.lightbend.lagom.javadsl.api.Descriptor
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.core.util.ReflectionUtils
import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.Explode
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.enums.ParameterStyle
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License
import io.swagger.v3.oas.annotations.links.Link
import io.swagger.v3.oas.annotations.links.LinkParameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.servers.Server
import io.swagger.v3.oas.annotations.servers.ServerVariable
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.servers.ServerVariables
import org.taymyr.lagom.openapi.Utils.Companion.modelConverter
import sun.misc.SharedSecrets
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class Utils {
    companion object {
        val modelConverter = ModelConverters.getInstance()!!
        init {
            modelConverter.addConverter(DoneModelConverter())
        }
    }
}

/**
 * Model utils
 */

fun ModelConverters.resolveAsResolvedSchema(cls: Type) = this.resolveAsResolvedSchema(AnnotatedType().type(cls))!!

fun io.swagger.v3.oas.models.Operation.findParameterByName(name: String): io.swagger.v3.oas.models.parameters.Parameter? {
    return this.parameters?.find { p -> p != null && p.name == name }
}

fun Operation.toModel(openAPI: OpenAPI): io.swagger.v3.oas.models.Operation {
    return io.swagger.v3.oas.models.Operation()
        .summary(this.summary.toModel())
        .description(this.description.toModel())
        .tags(tags.toModel())
        .requestBody(this.requestBody.toModel(openAPI))
        .externalDocs(this.externalDocs.toModel())
        .operationId(this.operationId.toModel())
        .parameters(this.parameters.toModel(openAPI))
        .responses(this.responses.toModel(openAPI))
        .deprecated(this.deprecated)
        .security(this.security.toModel())
        .servers(this.servers.toModel())
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

fun Array<ApiResponse>.toModel(openAPI: OpenAPI): io.swagger.v3.oas.models.responses.ApiResponses? {
    val ar = io.swagger.v3.oas.models.responses.ApiResponses()
    this.forEach { t -> ar.putAll(t.toModel(openAPI)) }
    return if (ar.isEmpty()) null else ar
}

fun ApiResponse.toModel(openAPI: OpenAPI): Map<String, io.swagger.v3.oas.models.responses.ApiResponse> {
    val response = io.swagger.v3.oas.models.responses.ApiResponse()
        .description(this.description)
        .headers(this.headers.toModel(openAPI))
        .content(this.content.toModel(openAPI))
        .`$ref`(this.ref.toModel())
    response.links = this.links.toModel()
    return if (response.content == null && response.`$ref` == null && response.description.isNullOrBlank()) emptyMap() else mapOf(this.responseCode to response)
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

fun Array<Header>.toModel(openAPI: OpenAPI): Map<String, io.swagger.v3.oas.models.headers.Header>? {
    val map = mutableMapOf<String, io.swagger.v3.oas.models.headers.Header>()
    this.map { t -> map.putAll(t.toModel(openAPI)) }
    return if (map.isEmpty()) null else map
}

fun Header.toModel(openAPI: OpenAPI): Map<String, io.swagger.v3.oas.models.headers.Header> {
    if (this.name.isBlank()) return emptyMap()
    val header = io.swagger.v3.oas.models.headers.Header()
        .description(this.description.toModel())
        .schema(this.schema.toModel(openAPI))
        .required(this.required)
        .deprecated(this.deprecated)
        .`$ref`(this.ref.toModel())
    return if (header.schema == null && header.`$ref` == null) emptyMap() else mapOf(this.name to header)
}

fun Array<Parameter>.toModel(openAPI: OpenAPI): List<io.swagger.v3.oas.models.parameters.Parameter?>? {
    val list = this.map { t -> t.toModel(openAPI) }.filter { t -> t != null }
    return if (list.isEmpty()) null else list
}

fun Parameter.toModel(openAPI: OpenAPI): io.swagger.v3.oas.models.parameters.Parameter? {
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
        .schema(this.schema.toModel(openAPI))
        .content(this.content.toModel(openAPI))
    if (this.`in` == ParameterIn.PATH) {
        parameter.required = true
    }
    return if (parameter.`in`.isNullOrBlank() || parameter.name.isNullOrBlank()) null else parameter
}

fun ParameterStyle.toModel(): io.swagger.v3.oas.models.parameters.Parameter.StyleEnum? {
    return when (this) {
        ParameterStyle.DEFAULT -> null
        else -> io.swagger.v3.oas.models.parameters.Parameter.StyleEnum.valueOf(this.toString())
    }
}

fun Explode.toModel(): Boolean? {
    return when (this) {
        Explode.DEFAULT -> null
        Explode.FALSE -> false
        Explode.TRUE -> true
    }
}

fun RequestBody.toModel(openAPI: OpenAPI): io.swagger.v3.oas.models.parameters.RequestBody? {
    val rb = io.swagger.v3.oas.models.parameters.RequestBody()
        .description(this.description.toModel())
        .content(this.content.toModel(openAPI))
        .required(this.required)
        .`$ref`(this.ref.toModel())
    return if ((rb.content == null || rb.content.isEmpty()) && rb.`$ref`.isNullOrBlank()) null else rb
}

fun Array<Content>.toModel(openAPI: OpenAPI): io.swagger.v3.oas.models.media.Content? {
    val content = io.swagger.v3.oas.models.media.Content()
    this.map { t -> t.toModel(openAPI) }.forEach { t -> t?.forEach { k, v -> content[k] = v } }
    return if (content.isEmpty()) null else content
}

fun Content.toModel(openAPI: OpenAPI): io.swagger.v3.oas.models.media.Content? {
    if (this.mediaType.isBlank()) return null
    val schema = this.schema.toModel(openAPI)
    val arraySchema = this.array.schema.toModel(openAPI)
    val resultSchema = if (schema == null && arraySchema != null) {
        ArraySchema().items(arraySchema)
    } else schema ?: return null
    return io.swagger.v3.oas.models.media.Content()
        .addMediaType(mediaType, MediaType().schema(resultSchema))
}

fun Schema.toModel(openAPI: OpenAPI): io.swagger.v3.oas.models.media.Schema<Any>? {
    val notSchema = if (this.not.java.isAssignableFrom(Void::class.java)) null else
        modelConverter.resolveAsResolvedSchema(this.not.java).schema
    val schema = if (this.implementation.java.isAssignableFrom(Void::class.java))
        io.swagger.v3.oas.models.media.Schema<Any>()
    else {
        val resolvedSchema = modelConverter.resolveAsResolvedSchema(this.implementation.java)
        resolvedSchema.referencedSchemas.forEach { name, schema -> run {
            if (openAPI.components.schemas == null || !openAPI.components.schemas.containsKey(name)) {
                openAPI.components.addSchemas(name, schema)
            }
        } }
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
    return if (schema.type.isNullOrBlank() && schema.`$ref`.isNullOrBlank()) null else schema
}

// TODO: extensions
fun Contact.toModel(): io.swagger.v3.oas.models.info.Contact? {
    val contact = io.swagger.v3.oas.models.info.Contact()
        .name(name)
        .url(url.toModel())
        .email(email.toModel())
    return if (contact.name.isBlank()) null else contact
}

// TODO: extensions
fun License.toModel(): io.swagger.v3.oas.models.info.License? {
    val license = io.swagger.v3.oas.models.info.License()
        .name(name)
        .url(url.toModel())
    return if (license.name.isBlank()) null else license
}

// TODO: extensions
fun Info.toModel() = io.swagger.v3.oas.models.info.Info()
    .title(title.toModel())
    .description(description.toModel())
    .termsOfService(termsOfService.toModel())
    .contact(contact.toModel())
    .license(license.toModel())
    .version(version.toModel())!!

// TODO: extensions
fun ExternalDocumentation.toModel(): io.swagger.v3.oas.models.ExternalDocumentation? {
    val doc = io.swagger.v3.oas.models.ExternalDocumentation()
        .description(description)
        .url(url.toModel())
    return if (doc.description.isBlank()) null else doc
}

// TODO: extensions
fun Tag.toModel(): io.swagger.v3.oas.models.tags.Tag? {
    val tag = io.swagger.v3.oas.models.tags.Tag()
        .name(name)
        .description(description.toModel())
        .externalDocs(externalDocs.toModel())
    return if (tag.name.isBlank()) null else tag
}

fun Array<Tag>.toModel(): List<io.swagger.v3.oas.models.tags.Tag?>? {
    val list = this.map { t -> t.toModel() }.filter { t -> t != null }
    return if (list.isEmpty()) null else list
}

// TODO: extensions
fun Server.toModel(): io.swagger.v3.oas.models.servers.Server? {
    val server = io.swagger.v3.oas.models.servers.Server()
        .url(url)
        .description(description.toModel())
        .variables(variables.toModel())
    return if (server.url.isNullOrBlank()) null else server
}

fun Array<Server>.toModel(): List<io.swagger.v3.oas.models.servers.Server?>? {
    val list = this.map { t -> t.toModel() }.filter { t -> t != null }
    return if (list.isEmpty()) null else list
}

// TODO: extensions
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

fun Array<String>.toModel(): List<String>? {
    val list = this.filter { t -> !t.isBlank() }
    return if (list.isEmpty()) null else list
}

fun String.toModel(): String? {
    return if (this.isBlank()) return null else this
}

/**
 * Reflect utils
 */

fun Class<*>.isAnnotationPresentInherited(aCls: Class<out Annotation>) = this.getAnnotationInherited(aCls) != null

fun <A : Annotation> Class<*>.getAnnotationInherited(aCls: Class<out A>): A? = ReflectionUtils.getAnnotation(this, aCls)

fun Class<*>.isArrayInterface() = this.isAssignableFrom(java.util.Collection::class.java) ||
    this.isAssignableFrom(List::class.java) ||
    this.isAssignableFrom(Set::class.java) ||
    this.isAssignableFrom(Collection::class.java)

fun MethodRefServiceCallHolder.toMethod(): Method {
    val pool = SharedSecrets.getJavaLangAccess().getConstantPool(this.methodReference()!!.javaClass)
    val size = pool.size
    for (i in 1..size) {
        try {
            val member = pool.getMethodAt(i)
            if (member is Method && !member.declaringClass.name.startsWith("java.lang")) {
                return member
            }
        } catch (t: Throwable) { }
    }
    throw IllegalArgumentException("Not a method reference")
}

fun Method.readModels() = this.genericReturnType.readModels()

fun Type.readModels(): List<TypeModel> {
    val resultList = mutableListOf<TypeModel>()
    when (this) {
        is ParameterizedType -> {
            val isArray = this.rawType is Class<*> && (this.rawType as Class<*>).isArrayInterface()
            this.actualTypeArguments.forEach { t -> run {
                when (t) {
                    is Class<*> -> {
                        resultList.add(TypeModel(t, isArray))
                    }
                    is ParameterizedType -> {
                        val rawType = t.rawType
                        val models = t.readModels()
                        if (rawType is Class<*> && rawType.isArrayInterface()) {
                            resultList.addAll(models.map { t -> run {
                                t.isArray = true
                                t
                            } })
                        } else {
                            resultList.addAll(models)
                        }
                    }
                    else -> { }
                }
            } }
        }
        is Class<*> -> resultList.add(TypeModel(this))
    }
    return resultList
}

/**
 * Lagom utils
 */

fun Descriptor.RestCallId.pathWithoutQueryParams(): String {
    return if (this.pathPattern().contains("?")) {
        this.pathPattern().substring(0, this.pathPattern().indexOf("?"))
    } else {
        this.pathPattern()
    }
}

fun Descriptor.RestCallId.swaggerTemplatedPath(): String {
    return Joiner.on("/").join(
        this.pathWithoutQueryParams().split("/")
            .map { segment -> if (segment.startsWith(":")) "{" + segment.substring(1) + "}" else segment }
    )
}

fun Descriptor.RestCallId.listOfQueryParams(): List<String> {
    return if (this.pathPattern().contains("?")) {
        this.pathPattern().substring(this.pathPattern().indexOf("?") + 1).split("&")
    } else {
        emptyList()
    }
}

fun Descriptor.RestCallId.listOfPathParams(): List<String> {
    return this.pathWithoutQueryParams().split("/")
        .filter { segment -> segment.startsWith(":") }
        .map { segment -> segment.substring(1) }
}
