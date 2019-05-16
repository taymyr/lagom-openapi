package org.taymyr.lagom.scaladsl.openapi.generate

import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.api.transport.ResponseHeader
import com.typesafe.config.ConfigFactory
import org.scalatest.Matchers
import org.scalatest.WordSpec
import org.taymyr.lagom.internal.openapi.TestUtils.resourceAsString
import org.taymyr.lagom.internal.openapi.TestUtils.yamlToJson
import org.taymyr.lagom.scaladsl.openapi.generate.empty.EmptyServiceImpl
import org.taymyr.lagom.scaladsl.openapi.generate.pets.PetsServiceImpl
import play.libs.Json

class GeneratorSpec extends WordSpec with Matchers {

  "Service without OpenAPIDefinition annotation" should {
    "return 404" in {
      val service = new EmptyServiceImpl(ConfigFactory.empty)
      val thrown  = the[NotFound] thrownBy service.openapi().invokeWithHeaders(null, null)
      thrown.getMessage shouldBe "OpenAPI specification not found"
      thrown.errorCode.http shouldBe 404
    }
  }

  "Service without OpenAPIDefinition" should {
    "generate yaml specification" in {
      val expected = Json.parse(yamlToJson(resourceAsString("pets.yml")))
      val service  = new PetsServiceImpl(ConfigFactory.empty)
      val result   = service.openapi().invokeWithHeaders(null, null)
      result.isCompleted shouldBe true
      val (header: ResponseHeader, spec: String) = result.value.get.get
      header.status shouldBe 200
      header.protocol.contentType shouldBe Some("application/x-yaml")
      Json.parse(yamlToJson(spec)) shouldBe expected
    }
  }

}
