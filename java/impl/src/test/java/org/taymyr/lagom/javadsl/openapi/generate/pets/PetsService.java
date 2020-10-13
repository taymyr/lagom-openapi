package org.taymyr.lagom.javadsl.openapi.generate.pets;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import org.taymyr.lagom.javadsl.openapi.LagomError;
import org.taymyr.lagom.javadsl.openapi.OpenAPIService;
import org.taymyr.lagom.javadsl.openapi.OpenAPIUtils;

import java.util.List;
import java.util.Optional;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;
import static com.lightbend.lagom.javadsl.api.Service.restCall;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;

@OpenAPIDefinition(
    info = @Info(
        version = "1.0.0",
        title = "Swagger Petstore",
        description = "A sample API that uses a petstore as an example to demonstrate features in the OpenAPI 3.0 specification",
        termsOfService = "http://swagger.io/terms/",
        contact = @Contact(
            name = "Swagger API Team",
            email = "apiteam@swagger.io",
            url = "http://swagger.io"
        ),
        license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html")
    ),
    servers = @Server(url = "http://petstore.swagger.io/api")
)
public interface PetsService extends OpenAPIService {

    @Operation(
        operationId = "find",
        description = "Returns all pets from the system that the user has access to",
        parameters = {
            @Parameter(
                name = "tags",
                description = "tags to filter by",
                in = QUERY,
                style = ParameterStyle.FORM,
                array = @ArraySchema(schema = @Schema(implementation = String.class))
            ),
            @Parameter(
                name = "limit",
                description = "maximum number of results to return",
                in = QUERY,
                schema = @Schema(implementation = Integer.class)
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "pet response",
                content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Pet.class))
                )
            ),
            @ApiResponse(
                description = "unexpected error",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LagomError.class)
                )
            )
        }
    )
    ServiceCall<NotUsed, List<Pet>> find(List<String> tags, Optional<Integer> limit);

    @Operation(
        operationId = "create",
        description = "Creates a new pet in the store.  Duplicates are allowed",
        requestBody = @RequestBody(
            description = "Pet to add to the store",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = NewPet.class)
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "pet response",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Pet.class)
                )
            ),
            @ApiResponse(
                description = "unexpected error",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LagomError.class)
                )
            )
        }
    )
    ServiceCall<NewPet, Pet> create();

    @Operation(
        operationId = "findById",
        description = "Returns a user based on a single ID, if the user does not have access to the pet",
        parameters = {
            @Parameter(
                name = "id",
                description = "ID of pet to fetch",
                in = PATH,
                required = true,
                schema = @Schema(implementation = Long.class)
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "pet response",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Pet.class)
                )
            ),
            @ApiResponse(
                description = "unexpected error",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LagomError.class)
                )
            )
        }
    )
    ServiceCall<NotUsed, Pet> findBy(Long id);

    @Operation(
        operationId = "delete",
        description = "deletes a single pet based on the ID supplied",
        parameters = {
            @Parameter(
                name = "id",
                description = "ID of pet to delete",
                in = PATH,
                required = true,
                schema = @Schema(implementation = Long.class)
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "pet deleted"
            ),
            @ApiResponse(
                description = "unexpected error",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LagomError.class)
                )
            )
        }
    )
    ServiceCall<NotUsed, NotUsed> delete(Long id);

    @Override
    default Descriptor descriptor() {
        return OpenAPIUtils.withOpenAPI(
            named("test").withCalls(
                pathCall("/pets?tags&limit", this::find),
                restCall(Method.POST, "/pets", this::create),
                pathCall("/pets/:id", this::findBy),
                restCall(Method.DELETE, "/pets/:id", this::delete)
            )
        );
    }

}
