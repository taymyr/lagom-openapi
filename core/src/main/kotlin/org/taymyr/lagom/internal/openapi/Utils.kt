@file:JvmName("Utils")

package org.taymyr.lagom.internal.openapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.IOException

private val jsonMapper = ObjectMapper()
private val yamlMapper = ObjectMapper(YAMLFactory())

fun yamlToJson(yaml: String?): String? = try {
    yaml?.let { jsonMapper.writeValueAsString(yamlMapper.readValue(it, Any::class.java)) }
} catch (e: IOException) {
    null
}

fun jsonToYaml(json: String?): String? = try {
    json?.let { yamlMapper.writeValueAsString(jsonMapper.readValue(it, Any::class.java)) }
} catch (e: IOException) {
    null
}
