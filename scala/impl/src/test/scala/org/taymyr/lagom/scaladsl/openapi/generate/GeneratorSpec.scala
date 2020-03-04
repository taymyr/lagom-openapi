package org.taymyr.lagom.scaladsl.openapi.generate

import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.taymyr.lagom.internal.openapi.TestUtils.resourceAsString
import org.taymyr.lagom.internal.openapi.Utils._
import org.taymyr.lagom.scaladsl.openapi.OpenAPISpec
import org.taymyr.lagom.scaladsl.openapi.generate.empty.EmptyServiceImpl
import org.taymyr.lagom.scaladsl.openapi.generate.pets.PetsServiceImpl
import play.libs.Json

class GeneratorSpec extends AnyWordSpec with OpenAPISpec with Matchers {

  "Service without OpenAPIDefinition annotation" should {
    "return 404" in {
      val service = new EmptyServiceImpl(ConfigFactory.empty)
      val thrown  = the[NotFound] thrownBy service.openapi(None).invokeWithHeaders(null, null)
      thrown.getMessage shouldBe "OpenAPI specification not found"
      thrown.errorCode.http shouldBe 404
    }
  }

  "Service without OpenAPIDefinition" should {
    "generate yaml specification" in {
      check(
        new PetsServiceImpl(ConfigFactory.empty),
        Json.parse(yamlToJson(resourceAsString("pets.yml")))
      )
    }
  }

}
