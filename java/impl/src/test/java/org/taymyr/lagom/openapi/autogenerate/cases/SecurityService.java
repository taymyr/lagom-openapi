package org.taymyr.lagom.openapi.autogenerate.cases;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import com.typesafe.config.Config;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.jetbrains.annotations.NotNull;
import org.taymyr.lagom.openapi.AbstractOpenAPIService;
import org.taymyr.lagom.openapi.OpenAPIService;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;


@SecurityScheme(
    name = "myOauth2Security",
    type = SecuritySchemeType.OAUTH2,
    in = SecuritySchemeIn.HEADER,
    description = "myOauthSecurity Description",
    flows = @OAuthFlows(
        implicit = @OAuthFlow(
            authorizationUrl = "http://x.com",
            scopes = @OAuthScope(
                name = "write:pets",
                description = "modify pets in your account"
            )
        )
    )
)
@SecurityRequirement(name = "security_key",
    scopes = {"write:pets", "read:pets"}
)
@SecurityRequirement(name = "myOauth2Security",
    scopes = {"write:pets"}
)
public interface SecurityService extends OpenAPIService {

    @Operation(
        responses = {
            @ApiResponse(
                description = "test description",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = String.class)
                )
            )
        },
        operationId = "Operation Id",
        description = "description"
    )
    @SecurityRequirement(
        name = "security_key",
        scopes = {"write:pets", "read:pets"}
    )
    ServiceCall<String, NotUsed> test1();

    @Operation(
        responses = {
            @ApiResponse(
                description = "test description",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = String.class)
                )
            )
        },
        operationId = "Operation Id 2",
        description = "description 2"
    )
    @SecurityRequirement(
        name = "security_key2",
        scopes = {"write:pets", "read:pets"}
    )
    ServiceCall<String, NotUsed> test2();

    @Override
    default Descriptor descriptor() {
        return withOpenAPI(
            named("test").withCalls(
                restCall(Method.GET, "/test1", this::test1),
                restCall(Method.GET, "/test2", this::test2)
            )
        );
    }

    class Impl extends AbstractOpenAPIService implements SecurityService {

        public Impl(@NotNull Config config) {
            super(config);
        }

        @Override
        public ServiceCall<String, NotUsed> test1() {
            return null;
        }

        @Override
        public ServiceCall<String, NotUsed> test2() {
            return null;
        }
    }

}
