package org.taymyr.lagom.scaladsl.openapi

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.Service.pathCall
import com.lightbend.lagom.scaladsl.api.Descriptor
import com.lightbend.lagom.scaladsl.api.ServiceCall

/**
 * OpenAPI service descriptor.
 */
trait OpenAPIService {

  /**
   * Return OpenAPI specification for current service.
   * @return OpenAPI specification
   */
  def openapi(format: Option[String]): ServiceCall[NotUsed, String]

  /**
   * @deprecated Use extension function withOpenAPI() instead.
   */
  @Deprecated
  def withOpenAPI(descriptor: Descriptor): Descriptor = descriptor.withOpenAPI()

  implicit class DescriptorWithOpenAPI(descriptor: Descriptor) {
    def withOpenAPI(): Descriptor = {
      descriptor.addCalls(
        pathCall(s"/_${descriptor.name}/openapi?format", openapi _)
      )
    }
  }
}
