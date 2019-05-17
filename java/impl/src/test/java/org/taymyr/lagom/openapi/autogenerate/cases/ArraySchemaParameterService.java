package org.taymyr.lagom.openapi.autogenerate.cases;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import com.typesafe.config.Config;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.jetbrains.annotations.NotNull;
import org.taymyr.lagom.openapi.AbstractOpenAPIService;
import org.taymyr.lagom.openapi.OpenAPIService;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

public interface ArraySchemaParameterService extends OpenAPIService {

    @Operation(
        operationId = "test",
        description = "array schema param test",
        parameters = {
            @Parameter(in = ParameterIn.QUERY, name = "arrayParameter", required = true, explode = Explode.TRUE,
                array = @ArraySchema(maxItems = 10, minItems = 1,
                    schema = @Schema(implementation = SubscriptionResponse.class),
                    uniqueItems = true
                )
            ),
        },
        responses = {
            @ApiResponse(
                description = "test description", content = @Content(
                mediaType = "*/*",
                schema =
                @Schema(
                    implementation = SubscriptionResponse.class)
            ))
        })
    ServiceCall<NotUsed, NotUsed> test();

    @Override
    default Descriptor descriptor() {
        return withOpenAPI(
            named("test").withCalls(
                restCall(Method.GET, "/test", this::test)
            )
        );
    }

    class Impl extends AbstractOpenAPIService implements ArraySchemaParameterService {

        public Impl(@NotNull Config config) {
            super(config);
        }

        @Override
        public ServiceCall<NotUsed, NotUsed> test() {
            return null;
        }

    }

    class SubscriptionResponse {
        public String subscriptionId;
    }

}
