package org.taymyr.lagom.scaladsl.openapi.file

import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.api.transport.ResponseHeader
import com.typesafe.config.ConfigFactory
import org.scalatest.Matchers
import org.scalatest.WordSpec
import org.taymyr.lagom.internal.openapi.TestUtils.resourceAsString

class StaticFileSpec extends WordSpec with Matchers {

  "Service without config" should {
    "return default json spec" in {
      val originalSpec = resourceAsString("test1.json")
      val service      = new Test1ServiceImpl(ConfigFactory.empty)
      val result       = service.openapi().invokeWithHeaders(null, null)
      result.isCompleted shouldBe true
      val (header: ResponseHeader, spec: String) = result.value.get.get
      header.status shouldBe 200
      header.protocol.contentType shouldBe Some("application/json")
      spec shouldBe originalSpec
    }

    "return default yaml spec" in {
      val originalSpec = resourceAsString("test2.yaml")
      val service      = new Test2ServiceImpl(ConfigFactory.empty)
      val result       = service.openapi().invokeWithHeaders(null, null)
      result.isCompleted shouldBe true
      val (header: ResponseHeader, spec: String) = result.value.get.get
      header.status shouldBe 200
      header.protocol.contentType shouldBe Some("application/x-yaml")
      spec shouldBe originalSpec
    }

    "return default yml spec" in {
      val originalSpec = resourceAsString("test3.yml")
      val service      = new Test3ServiceImpl(ConfigFactory.empty)
      val result       = service.openapi().invokeWithHeaders(null, null)
      result.isCompleted shouldBe true
      val (header: ResponseHeader, spec: String) = result.value.get.get
      header.status shouldBe 200
      header.protocol.contentType shouldBe Some("application/x-yaml")
      spec shouldBe originalSpec
    }
  }

  "Service with incorrect config" should {
    "return 404" in {
      val config  = ConfigFactory.parseString("openapi.file = file")
      val service = new Test1ServiceImpl(config)
      val thrown  = the[NotFound] thrownBy service.openapi().invokeWithHeaders(null, null)
      thrown.getMessage shouldBe "OpenAPI specification not found"
      thrown.errorCode.http shouldBe 404
    }
  }

  "Service with correct config" should {
    "return spec" in {
      val originalSpec = resourceAsString("config.json")
      val config       = ConfigFactory.parseString("openapi.file = config.json")
      val service      = new Test1ServiceImpl(config)
      val result       = service.openapi().invokeWithHeaders(null, null)
      result.isCompleted shouldBe true
      val (header: ResponseHeader, spec: String) = result.value.get.get
      header.status shouldBe 200
      header.protocol.contentType shouldBe Some("application/json")
      spec shouldBe originalSpec
    }
  }
}
