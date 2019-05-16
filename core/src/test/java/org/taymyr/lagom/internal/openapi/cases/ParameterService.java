package org.taymyr.lagom.internal.openapi.cases;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Schema;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

@OpenAPIDefinition(
    info = @Info(title = "Test", version = "1.0.0", description = "Description")
)
public interface ParameterService extends Service {

    @Operation(parameters = {
        @Parameter(
            in = ParameterIn.PATH,
            name = "id",
            required = true,
            description = "parameter description",
            allowReserved = true,
            style = ParameterStyle.SIMPLE,
            schema = @Schema(
                type = "string",
                format = "uuid",
                description = "the generated UUID",
                accessMode = Schema.AccessMode.READ_ONLY)
        )}
    )
    ServiceCall<NotUsed, NotUsed> test(String id);

    @Override
    default Descriptor descriptor() {
        return named("test").withCalls(
            restCall(Method.GET, "/test/:id", this::test)
        );
    }

}
