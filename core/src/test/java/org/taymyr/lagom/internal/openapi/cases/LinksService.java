package org.taymyr.lagom.internal.openapi.cases;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.links.LinkParameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;

@OpenAPIDefinition(
    info = @Info(title = "Test", version = "1.0.0", description = "Description")
)
public interface LinksService extends Service {

    @Operation(
        operationId = "getUserWithAddress",
        parameters = @Parameter(
            description = "User identity",
            name = "userId",
            in = QUERY,
            schema = @Schema(implementation = String.class)
        ),
        responses = {
            @ApiResponse(
                description = "test description",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = User.class)
                ),
                links = {
                    @Link(
                        name = "Link",
                        operationId = "getAddress",
                        parameters = @LinkParameter(
                            name = "userId",
                            expression = "$request.query.userId"
                        ))
                })
        }
    )
    ServiceCall<NotUsed, User> test(String userId);

    @Override
    default Descriptor descriptor() {
        return named("test").withCalls(
            restCall(Method.GET, "/test/:userId", this::test)
        );
    }
}
