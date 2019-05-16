package org.taymyr.lagom.scaladsl.openapi.generate.pets

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.Service.named
import com.lightbend.lagom.scaladsl.api.Service.pathCall
import com.lightbend.lagom.scaladsl.api.Service.restCall
import com.lightbend.lagom.scaladsl.api.Descriptor
import com.lightbend.lagom.scaladsl.api.Service
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.Method
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn.PATH
import io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY
import io.swagger.v3.oas.annotations.enums.ParameterStyle
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.servers.Server
import org.taymyr.lagom.scaladsl.openapi.OpenAPIService

@OpenAPIDefinition(
  info = new Info(
    version = "1.0.0",
    title = "Swagger Petstore",
    description = "A sample API that uses a petstore as an example to demonstrate features in the OpenAPI 3.0 specification",
    termsOfService = "http://swagger.io/terms/",
    contact = new Contact(name = "Swagger API Team", email = "apiteam@swagger.io", url = "http://swagger.io"),
    license = new License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html")
  ),
  servers = Array(new Server(url = "http://petstore.swagger.io/api"))
)
trait PetsService extends OpenAPIService with Service {

  @Operation(
    operationId = "find",
    description = "Returns all pets from the system that the user has access to",
    parameters = Array(
      new Parameter(
        name = "tags",
        description = "tags to filter by",
        in = QUERY,
        style = ParameterStyle.FORM,
        array = new ArraySchema(schema = new Schema(implementation = classOf[String]))
      ),
      new Parameter(
        name = "limit",
        description = "maximum number of results to return",
        in = QUERY,
        schema = new Schema(implementation = classOf[Int])
      )
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "pet response",
        content =
          Array(new Content(mediaType = "application/json", array = new ArraySchema(schema = new Schema(implementation = classOf[Pet]))))
      ),
      new ApiResponse(
        description = "unexpected error",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[Error])))
      )
    )
  )
  def find(tags: List[String], limit: Option[Int]): ServiceCall[NotUsed, List[Pet]]

  @Operation(
    operationId = "create",
    description = "Creates a new pet in the store.  Duplicates are allowed",
    requestBody = new RequestBody(
      description = "Pet to add to the store",
      required = true,
      content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[NewPet])))
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "pet response",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[Pet])))
      ),
      new ApiResponse(
        description = "unexpected error",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[Error])))
      )
    )
  )
  def create: ServiceCall[NewPet, Pet]

  @Operation(
    operationId = "findById",
    description = "Returns a user based on a single ID, if the user does not have access to the pet",
    parameters = Array(
      new Parameter(
        name = "id",
        description = "ID of pet to fetch",
        in = PATH,
        required = true,
        schema = new Schema(implementation = classOf[Long])
      )
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "pet response",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[Pet])))
      ),
      new ApiResponse(
        description = "unexpected error",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[Error])))
      )
    )
  )
  def findBy(id: Long): ServiceCall[NotUsed, Pet]

  @Operation(
    operationId = "delete",
    description = "deletes a single pet based on the ID supplied",
    parameters = Array(
      new Parameter(
        name = "id",
        description = "ID of pet to delete",
        in = PATH,
        required = true,
        schema = new Schema(implementation = classOf[Long])
      )
    ),
    responses = Array(
      new ApiResponse(responseCode = "204", description = "pet deleted"),
      new ApiResponse(
        description = "unexpected error",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[Error])))
      )
    )
  )
  def delete(id: Long): ServiceCall[NotUsed, NotUsed]

  override def descriptor: Descriptor =
    withOpenAPI(
      named("test").withCalls(
        pathCall("/pets?tags&limit", find _),
        restCall(Method.POST, "/pets", create),
        pathCall("/pets/:id", findBy _),
        restCall(Method.DELETE, "/pets/:id", delete _)
      )
    )

}
