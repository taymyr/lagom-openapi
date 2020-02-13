package org.taymyr.lagom.javadsl.openapi.generate;

import akka.japi.Pair;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taymyr.lagom.javadsl.openapi.AbstractOpenAPIServiceKt;
import org.taymyr.lagom.javadsl.openapi.generate.empty.EmptyServiceImpl;
import org.taymyr.lagom.javadsl.openapi.generate.pets.PetsServiceImpl;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.typesafe.config.ConfigFactory.load;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.taymyr.lagom.internal.openapi.TestUtils.eventually;
import static org.taymyr.lagom.internal.openapi.TestUtils.resourceAsString;
import static org.taymyr.lagom.internal.openapi.TestUtils.yamlToJson;

class GeneratorTest {

    @Test
    @DisplayName("Service without OpenAPIDefinition annotation should return 404")
    void shouldReturn404WithoutAnnotation() {
        EmptyServiceImpl service = new EmptyServiceImpl(load());
        assertThatExceptionOfType(NotFound.class)
            .isThrownBy(() -> service.openapi().invokeWithHeaders(null, null))
            .withMessage("OpenAPI specification not found")
            .has(new Condition<>(thr -> thr.errorCode().http() == 404, "Error code must be 404"));
    }

    @Test
    @DisplayName("Service without OpenAPIDefinition should generate yaml specification")
    void shouldNormalGenerateYaml() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        String expected = yamlToJson(resourceAsString("pets.yml"));
        PetsServiceImpl service = new PetsServiceImpl(load());
        Pair<ResponseHeader, String> openapi = eventually(service.openapi().invokeWithHeaders(null, null));
        assertThat(openapi.first().status()).isEqualTo(200);
        assertThat(openapi.first().protocol()).isEqualTo(AbstractOpenAPIServiceKt.getYAML());
        assertThatJson(yamlToJson(openapi.second())).isEqualTo(expected);
    }

}

