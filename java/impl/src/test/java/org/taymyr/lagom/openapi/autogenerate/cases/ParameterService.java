package org.taymyr.lagom.openapi.autogenerate.cases;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import com.typesafe.config.Config;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jetbrains.annotations.NotNull;
import org.taymyr.lagom.openapi.AbstractOpenAPIService;
import org.taymyr.lagom.openapi.OpenAPIService;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

public interface ParameterService extends OpenAPIService {

    @Operation(parameters = {
        @Parameter(in = ParameterIn.PATH, name = "subscriptionId",
            required = true, description = "parameter description",
            allowReserved = true,
            style = ParameterStyle.SIMPLE,
            schema = @Schema(
                type = "string",
                format = "uuid",
                description = "the generated UUID",
                accessMode = Schema.AccessMode.READ_ONLY)
        )}
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

    class Impl extends AbstractOpenAPIService implements ParameterService {

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
