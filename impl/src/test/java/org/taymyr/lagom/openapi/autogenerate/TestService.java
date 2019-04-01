package org.taymyr.lagom.openapi.autogenerate;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.taymyr.lagom.openapi.OpenAPIService;

import java.util.Optional;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;

@OpenAPIDefinition(
    info = @Info(
        version = "1.0.0",
        title = "Swagger Petstore",
        license = @License(
            name = "MIT"
        )
    ),
    servers = @Server(
        url = "http://petstore.swagger.io/v1"
    ),
    tags = @Tag(name = "pets")
)
public interface TestService extends OpenAPIService {

    @Operation(
        summary = "List all pets",
        tags = "pets",
        parameters = @Parameter(in = QUERY, name = "limit", description = "How many items to return at one time (max 100)"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Pets.class)
                ),
                description = "A paged array of pets",
                headers = @Header(name = "x-next", description = "A link to the next page of responses", schema = @Schema(
                    implementation = String.class
                ))
            ),
            @ApiResponse(
                description = "unexpected error",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Error.class)
                )
            )
        })
    ServiceCall<NotUsed, Pets> listPets(Optional<Integer> limit);

    @Operation(
        summary = "Create a pet",
        tags = "pets",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Null response"
            ),
            @ApiResponse(
                description = "Unexpected error",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Error.class)
                )
            )
        }
    )
    ServiceCall<NotUsed, NotUsed> createPets();

    @Operation(
        summary = "Info for a specific pet",
        tags = "pets",
        parameters = @Parameter(in = PATH, name = "petId", description = "The id of the pet to retrieve"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Pets.class)
                ),
                description = "Expected response to a valid request"
            ),
            @ApiResponse(
                description = "unexpected error",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Error.class)
                )
            )
        }
    )
    ServiceCall<NotUsed, Pets> showPetById(String petId);

    @Override
    default Descriptor descriptor() {
        return withOpenAPI(
            named("test").withCalls(
                restCall(Method.GET, "/pets?limit", this::listPets),
                restCall(Method.POST, "/pets", this::createPets),
                restCall(Method.GET, "/pets/:petId", this::showPetById)
            )
        );
    }
}
