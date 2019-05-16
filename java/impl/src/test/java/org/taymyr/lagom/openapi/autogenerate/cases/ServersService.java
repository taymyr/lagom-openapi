package org.taymyr.lagom.openapi.autogenerate.cases;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import com.typesafe.config.Config;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.ServerVariable;
import org.jetbrains.annotations.NotNull;
import org.taymyr.lagom.openapi.AbstractOpenAPIService;
import org.taymyr.lagom.openapi.OpenAPIService;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;


@OpenAPIDefinition(
    info = @Info(
        version = "1.0.0",
        title = "Test",
        license = @License(
            name = "MIT"
        )
    ),
    servers = {
        @Server(
            description = "definition server 1",
            url = "http://definition1",
            variables = {
                @ServerVariable(name = "var1", description = "var 1", defaultValue = "1", allowableValues = {"1", "2"}),
                @ServerVariable(name = "var2", description = "var 2", defaultValue = "1", allowableValues = {"1", "2"})
            })
    }
)
@Server(
    description = "class server 1",
    url = "http://class1",
    variables = {
        @ServerVariable(name = "var1", description = "var 1", defaultValue = "1", allowableValues = {"1", "2"}),
        @ServerVariable(name = "var2", description = "var 2", defaultValue = "1", allowableValues = {"1", "2"})
    })
@Server(
    description = "class server 2",
    url = "http://class2",
    variables = {
        @ServerVariable(name = "var1", description = "var 1", defaultValue = "1", allowableValues = {"1", "2"})
    })
public interface ServersService extends OpenAPIService {

    @Operation(
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Null response"
            )
        },
        servers = {
        @Server(
            description = "operation server 1",
            url = "http://op1",
            variables = {
                @ServerVariable(name = "var1", description = "var 1", defaultValue = "1", allowableValues = {"1", "2"})
            })
    })
    @Server(
        description = "method server 1",
        url = "http://method1",
        variables = {
            @ServerVariable(name = "var1", description = "var 1", defaultValue = "1", allowableValues = {"1", "2"})
        })
    @Server(
        description = "method server 2",
        url = "http://method2"
    )
    ServiceCall<NotUsed, NotUsed> test();

    @Override
    default Descriptor descriptor() {
        return withOpenAPI(
            named("test").withCalls(
                restCall(Method.GET, "/test1", this::test)
            )
        );
    }

    class Impl extends AbstractOpenAPIService implements ServersService {

        public Impl(@NotNull Config config) {
            super(config);
        }

        @Override
        public ServiceCall<NotUsed, NotUsed> test() {
            return null;
        }

    }

}
