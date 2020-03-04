package org.taymyr.lagom.scaladsl.openapi.file

import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.typesafe.config.ConfigFactory
import org.scalatest.wordspec.AnyWordSpec
import org.taymyr.lagom.internal.openapi.TestUtils.resourceAsString
import org.taymyr.lagom.internal.openapi.Utils._
import org.taymyr.lagom.scaladsl.openapi.OpenAPISpec
import play.libs.Json

class StaticFileSpec extends AnyWordSpec with OpenAPISpec {

  "Service without config" should {
    "return default json spec" in {
      check(
        new Test1ServiceImpl(ConfigFactory.empty),
        Json.parse(resourceAsString("test1.json"))
      )
    }

    "return default yaml spec" in {
      check(
        new Test2ServiceImpl(ConfigFactory.empty),
        Json.parse(yamlToJson(resourceAsString("test2.yaml")))
      )
    }

    "return default yml spec" in {
      check(
        new Test3ServiceImpl(ConfigFactory.empty),
        Json.parse(yamlToJson(resourceAsString("test3.yml")))
      )
    }
  }

  "Service with incorrect config" should {
    "return 404" in {
      val config  = ConfigFactory.parseString("openapi.file = file")
      val service = new Test1ServiceImpl(config)
      val thrown  = the[NotFound] thrownBy service.openapi(Some("json")).invokeWithHeaders(null, null)
      thrown.getMessage shouldBe "OpenAPI specification not found"
      thrown.errorCode.http shouldBe 404
    }
  }

  "Service with correct config" should {
    "return spec" in {
      check(
        new Test1ServiceImpl(ConfigFactory.parseString("openapi.file = config.json")),
        Json.parse(resourceAsString("config.json"))
      )
    }
  }
}
