package org.taymyr.lagom.openapi.autogenerate.cases;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import com.typesafe.config.Config;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.jetbrains.annotations.NotNull;
import org.taymyr.lagom.openapi.AbstractOpenAPIService;
import org.taymyr.lagom.openapi.OpenAPIService;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

@SecurityScheme(name = "myOauth2Security",
        type = SecuritySchemeType.OAUTH2,
        in = SecuritySchemeIn.HEADER,
        description = "myOauthSecurity Description",
        ref = "Security",
        flows = @OAuthFlows(implicit = @OAuthFlow(authorizationUrl = "http://x.com",
                scopes = @OAuthScope(
                        name = "write:pets",
                        description = "modify pets in your account"))
        )
)
public interface RefSecurityService extends OpenAPIService {

    @Operation(
        operationId = "test",
        summary = "Secured method example",
        tags = "cases",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Null response"
            )
        }
    )
    ServiceCall<NotUsed, NotUsed> secured();

    @Override
    default Descriptor descriptor() {
        return withOpenAPI(
            named("test").withCalls(
                restCall(Method.GET, "/cases", this::secured)
            )
        );
    }

    class Impl extends AbstractOpenAPIService implements RefSecurityService {

        public Impl(@NotNull Config config) {
            super(config);
        }

        @Override
        public ServiceCall<NotUsed, NotUsed> secured() {
            return null;
        }
    }

}
