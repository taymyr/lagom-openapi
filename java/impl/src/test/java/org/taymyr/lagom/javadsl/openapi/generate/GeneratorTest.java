package org.taymyr.lagom.javadsl.openapi.generate;

import com.lightbend.lagom.javadsl.api.transport.NotFound;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taymyr.lagom.javadsl.openapi.AbstractTest;
import org.taymyr.lagom.javadsl.openapi.generate.empty.EmptyServiceImpl;
import org.taymyr.lagom.javadsl.openapi.generate.pets.PetsServiceImpl;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.typesafe.config.ConfigFactory.load;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.taymyr.lagom.internal.openapi.TestUtils.resourceAsString;
import static org.taymyr.lagom.internal.openapi.Utils.yamlToJson;

class GeneratorTest extends AbstractTest {

    @Test
    @DisplayName("Service without OpenAPIDefinition annotation should return 404")
    void shouldReturn404WithoutAnnotation() {
        EmptyServiceImpl service = new EmptyServiceImpl(load());
        assertThatExceptionOfType(NotFound.class)
            .isThrownBy(() -> service.openapi(empty()).invokeWithHeaders(null, null))
            .withMessage("OpenAPI specification not found")
            .has(new Condition<>(thr -> thr.errorCode().http() == 404, "Error code must be 404"));
    }

    @Test
    @DisplayName("Service with OpenAPIDefinition should generate specification")
    void shouldNormalGenerateYaml() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        check(
            new PetsServiceImpl(load()),
            yamlToJson(resourceAsString("pets.yml"))
        );
    }

}

