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

  def withOpenAPI(descriptor: Descriptor): Descriptor = {
    descriptor.addCalls(
      pathCall(s"/_${descriptor.name}/openapi?format", openapi _)
    )
  }

}
