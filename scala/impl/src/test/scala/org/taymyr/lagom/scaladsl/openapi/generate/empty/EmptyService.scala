package org.taymyr.lagom.scaladsl.openapi.generate.empty

import com.lightbend.lagom.scaladsl.api.Descriptor
import com.lightbend.lagom.scaladsl.api.Service
import com.typesafe.config.Config
import org.taymyr.lagom.scaladsl.openapi.OpenAPIService
import org.taymyr.lagom.scaladsl.openapi.OpenAPIServiceImpl

trait EmptyService extends OpenAPIService with Service {
  override def descriptor: Descriptor = withOpenAPI(Service.named("test"))
}

class EmptyServiceImpl(override val config: Config) extends EmptyService with OpenAPIServiceImpl {}
