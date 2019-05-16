package org.taymyr.lagom.openapi.autogenerate.cases;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import com.typesafe.config.Config;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.links.LinkParameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.jetbrains.annotations.NotNull;
import org.taymyr.lagom.openapi.AbstractOpenAPIService;
import org.taymyr.lagom.openapi.OpenAPIService;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;

public interface RefLinkService extends OpenAPIService {

    @Operation(
        operationId = "getUserWithAddress",
        responses = {
            @ApiResponse(
                description = "test description",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = User.class)
                ),
                links = {
                    @Link(
                        name = "address",
                        operationId = "getAddress",
                        ref = "Link",
                        parameters = @LinkParameter(
                            name = "userId",
                            expression = "$request.query.userId"
                        ))
                })
        },
        parameters = @Parameter(
            description = "User identity",
            name = "userId",
            in = QUERY,
            schema = @Schema(implementation = String.class)
        )
    )
    ServiceCall<User, NotUsed> test(String userId);

    @Override
    default Descriptor descriptor() {
        return withOpenAPI(
            named("test").withCalls(
                restCall(Method.GET, "/test", this::test)
            )
        );
    }

    class Impl extends AbstractOpenAPIService implements RefLinkService {

        public Impl(@NotNull Config config) {
            super(config);
        }

        @Override
        public ServiceCall<User, NotUsed> test(String userId) {
            return null;
        }
    }

}
