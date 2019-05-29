package org.taymyr.lagom.scaladsl.openapi.file

import com.lightbend.lagom.scaladsl.api.Descriptor
import com.lightbend.lagom.scaladsl.api.Service
import com.typesafe.config.Config
import org.taymyr.lagom.scaladsl.openapi.OpenAPIService
import org.taymyr.lagom.scaladsl.openapi.OpenAPIServiceImpl

trait Test1Service extends OpenAPIService with Service {
  override def descriptor: Descriptor = withOpenAPI(Service.named("test1"))
}
class Test1ServiceImpl(override val config: Config) extends Test1Service with OpenAPIServiceImpl {}

trait Test2Service extends OpenAPIService with Service {
  override def descriptor: Descriptor = withOpenAPI(Service.named("test2"))
}
class Test2ServiceImpl(override val config: Config) extends Test2Service with OpenAPIServiceImpl {}

trait Test3Service extends OpenAPIService with Service {
  override def descriptor: Descriptor = withOpenAPI(Service.named("test3"))
}
class Test3ServiceImpl(override val config: Config) extends Test3Service with OpenAPIServiceImpl {}
