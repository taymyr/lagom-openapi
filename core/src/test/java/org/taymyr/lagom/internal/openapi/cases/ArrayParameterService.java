package org.taymyr.lagom.internal.openapi.cases;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

@OpenAPIDefinition(
    info = @Info(title = "Test", version = "1.0.0", description = "Description")
)
public interface ArrayParameterService extends Service {

    @Operation(
        operationId = "test",
        description = "array schema param test",
        parameters = {
            @Parameter(in = ParameterIn.QUERY, name = "params", required = true, explode = Explode.TRUE,
                array = @ArraySchema(maxItems = 10, minItems = 1,
                    schema = @Schema(implementation = String.class),
                    uniqueItems = true
                )
            ),
        }
    )
    ServiceCall<NotUsed, NotUsed> test(List<String> params);

    @Override
    default Descriptor descriptor() {
        return named("test").withCalls(
            restCall(Method.GET, "/test?params", this::test)
        );
    }

}
