package org.taymyr.lagom.internal.openapi.cases;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@OpenAPIDefinition(
    info = @Info(
        title = "User service",
        description = "User service",
        version = "1.0.0"
    )
)
public interface RequestBodyService extends Service {

    @Operation(
        summary = "Creates a new user.",
        requestBody = @RequestBody(
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "User created")
        })
    ServiceCall<User, NotUsed> create();

    @Override
    default Descriptor descriptor() {
        return Service.named("test").withCalls(
            Service.restCall(Method.POST, "/user", this::create)
        );
    }
}
