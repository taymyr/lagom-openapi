package org.taymyr.lagom.openapi.autogenerate.cases;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import com.typesafe.config.Config;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.jetbrains.annotations.NotNull;
import org.taymyr.lagom.openapi.AbstractOpenAPIService;
import org.taymyr.lagom.openapi.OpenAPIService;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

/**
 * @author Evgeny Stankevich {@literal <stankevich.evg@gmail.com>}.
 */
public interface RequestExampleService extends OpenAPIService {

    @Operation(
        requestBody = @RequestBody(
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class),
                examples = @ExampleObject(value = "{\"foo\" : \"foo\", \"bar\" : \"bar\"}")
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Null response"
            )
        })
    ServiceCall<NotUsed, NotUsed> example1();

    @Override
    default Descriptor descriptor() {
        return withOpenAPI(
            named("test").withCalls(
                restCall(Method.POST, "/test1", this::example1)
            )
        );
    }

    class Impl extends AbstractOpenAPIService implements RequestExampleService {

        public Impl(@NotNull Config config) {
            super(config);
        }

        @Override
        public ServiceCall<NotUsed, NotUsed> example1() {
            return null;
        }

    }

}
