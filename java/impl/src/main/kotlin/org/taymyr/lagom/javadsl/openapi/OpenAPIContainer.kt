package org.taymyr.lagom.javadsl.openapi

import com.lightbend.lagom.javadsl.api.Service
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol
import com.typesafe.config.Config
import io.swagger.v3.core.util.Json
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import org.slf4j.LoggerFactory
import org.taymyr.lagom.internal.openapi.isAnnotationPresentInherited
import org.taymyr.lagom.internal.openapi.jsonToYaml
import org.taymyr.lagom.internal.openapi.yamlToJson

internal class OpenAPIContainer(service: Service, config: Config?) {

    data class OpenAPISpec(val json: String?, var yaml: String?)

    val spec: OpenAPISpec by lazy {
        val isAnnotated: Boolean = service.javaClass.isAnnotationPresentInherited(OpenAPIDefinition::class.java)
        if (isAnnotated) {
            generateSpecResource(service)
        } else {
            createSpecResponseFromResource(service, config)
        }
    }

    companion object {
        private const val SPEC_CONFIG_PATH = "openapi.file"

        private val log = LoggerFactory.getLogger(OpenAPIContainer::class.java)

        fun generateSpecResource(service: Service): OpenAPISpec {
            val api = SpecGenerator().generate(service)
            return OpenAPISpec(Json.pretty(api), Yaml.pretty(api))
        }

        private fun fromFile(file: String, default: MessageProtocol): MessageProtocol =
            when (file.substring(file.lastIndexOf(".") + 1)) {
                "json" -> JSON
                "yaml", "yml" -> YAML
                else -> default
            }

        fun createSpecResponseFromResource(service: Service, config: Config?): OpenAPISpec {
            var spec: String? = null
            var protocol: MessageProtocol? = null
            val paths = if (config != null && config.hasPath(SPEC_CONFIG_PATH)) {
                listOf(config.getString(SPEC_CONFIG_PATH))
            } else {
                listOf("json", "yaml", "yml").map { "${service.descriptor().name()}.$it" }
            }
            for (filename in paths) {
                try {
                    val openapiSpec = service.javaClass.getResource("/$filename")
                    spec = openapiSpec?.readText()
                    spec ?: continue
                    protocol = fromFile(filename, YAML)
                    log.info("Load OpenAPI specification from {}", openapiSpec)
                    break
                } catch (e: Exception) {
                }
            }
            if (spec == null) log.error("OpenAPI specification not found in {}", paths)
            return when (protocol) {
                JSON -> OpenAPISpec(spec, jsonToYaml(spec))
                YAML -> OpenAPISpec(yamlToJson(spec), spec)
                else -> OpenAPISpec(null, null)
            }
        }
    }
}
