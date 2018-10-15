package org.taymyr.lagom.openapi

import com.lightbend.lagom.javadsl.api.transport.NotFound
import com.typesafe.config.ConfigFactory
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import io.kotlintest.whenReady
import org.taymyr.lagom.javadsl.api.transport.MessageProtocols.YAML
import org.taymyr.lagom.javadsl.api.transport.ResponseHeaders.OK_JSON

class OpenAPIServiceTest : StringSpec({

    "Service with incorrect config should be return 404" {
        val config = ConfigFactory.parseString("openapi.file = file")
        val service = Test1ServiceImpl(config)
        val exception = shouldThrow<NotFound> {
            service.openapi().invokeWithHeaders(null, null)
        }
        exception.message shouldBe "OpenAPI specification not found"
        exception.errorCode().http() shouldBe 404
    }

    "Service with correct config should be return spec" {
        val originalSpec = this.javaClass.getResource("/config.json").readText()
        val config = ConfigFactory.parseString("openapi.file = config.json")
        val service = Test1ServiceImpl(config)
        whenReady(service.openapi().invokeWithHeaders(null, null).toCompletableFuture()) {
            it.first() shouldBe OK_JSON
            it.second() shouldBe originalSpec
        }
    }

    "Service without config should be return default json spec" {
        val originalSpec = this.javaClass.getResource("/test1.json").readText()
        val service = Test1ServiceImpl(ConfigFactory.empty())
        whenReady(service.openapi().invokeWithHeaders(null, null).toCompletableFuture()) {
            it.first() shouldBe OK_JSON
            it.second() shouldBe originalSpec
        }
    }

    "Service without config should be return default yaml spec" {
        val originalSpec = this.javaClass.getResource("/test2.yaml").readText()
        val service = Test2ServiceImpl(ConfigFactory.empty())
        whenReady(service.openapi().invokeWithHeaders(null, null).toCompletableFuture()) {
            it.first().status() shouldBe 200
            it.first().protocol() shouldBe YAML
            it.second() shouldBe originalSpec
        }
    }

    "Service without config should be return default yml spec" {
        val originalSpec = this.javaClass.getResource("/test3.yml").readText()
        val service = Test3ServiceImpl(ConfigFactory.empty())
        whenReady(service.openapi().invokeWithHeaders(null, null).toCompletableFuture()) {
            it.first().status() shouldBe 200
            it.first().protocol() shouldBe YAML
            it.second() shouldBe originalSpec
        }
    }
})