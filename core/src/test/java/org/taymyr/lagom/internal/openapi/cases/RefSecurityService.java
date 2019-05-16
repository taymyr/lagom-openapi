package org.taymyr.lagom.internal.openapi.cases;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

@OpenAPIDefinition(
    info = @Info(
        version = "0.0.1",
        title = "test",
        description = "info"
    )
)
@SecurityScheme(
    name = "Security",
    type = SecuritySchemeType.OAUTH2,
    description = "Security Example"
)
@SecurityScheme(
    name = "myOauth2Security",
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
public interface RefSecurityService extends Service {

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
        return named("test").withCalls(
            restCall(Method.GET, "/cases", this::secured)
        );
    }
}
