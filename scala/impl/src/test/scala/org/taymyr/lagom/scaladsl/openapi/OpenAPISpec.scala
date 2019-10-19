package org.taymyr.lagom.scaladsl.openapi

import com.fasterxml.jackson.databind.JsonNode
import org.scalatest.Matchers
import play.libs.Json
import org.taymyr.lagom.internal.openapi.Utils._

trait OpenAPISpec extends Matchers {

  protected def check(service: OpenAPIServiceImpl, expectedJson: JsonNode) = {
    var result = service.openapi(Some("JsOn")).invokeWithHeaders(null, null)
    result.isCompleted shouldBe true
    var headerSpec = result.value.get.get
    headerSpec._1.status shouldBe 200
    headerSpec._1.protocol.contentType shouldBe Some("application/json")
    Json.parse(headerSpec._2) shouldBe expectedJson

    result = service.openapi(None).invokeWithHeaders(null, null)
    result.isCompleted shouldBe true
    headerSpec = result.value.get.get
    headerSpec._1.status shouldBe 200
    headerSpec._1.protocol.contentType shouldBe Some("application/x-yaml")
    Json.parse(yamlToJson(headerSpec._2)) shouldBe expectedJson
  }

}
