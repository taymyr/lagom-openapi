package org.taymyr.lagom.javadsl.openapi.file;

import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taymyr.lagom.javadsl.openapi.AbstractTest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.taymyr.lagom.internal.openapi.TestUtils.resourceAsString;
import static org.taymyr.lagom.internal.openapi.Utils.yamlToJson;

class StaticFileTest extends AbstractTest {

    @Test
    @DisplayName("Service with incorrect config should be return 404")
    void shouldReturn404() {
        Config config = ConfigFactory.parseString("openapi.file = file");
        Test1ServiceImpl service = new Test1ServiceImpl(config);
        assertThatExceptionOfType(NotFound.class)
            .isThrownBy(() -> service.openapi(empty()).invokeWithHeaders(null, null))
            .withMessage("OpenAPI specification not found")
            .has(new Condition<>(thr -> thr.errorCode().http() == 404, "Error code must be 404"));
    }

    @Test
    @DisplayName("Service with correct config should be return spec")
    void serviceWithCorrectConfig() throws InterruptedException, ExecutionException, TimeoutException {
        check(
            new Test1ServiceImpl(ConfigFactory.parseString("openapi.file = config.json")),
            resourceAsString("config.json")
        );
    }

    @Test
    @DisplayName("Service without config should be return default json spec")
    void serviceWithDefaultJsonSpec() throws InterruptedException, ExecutionException, TimeoutException {
        check(
            new Test1ServiceImpl(ConfigFactory.load()),
            resourceAsString("test1.json")
        );
    }

    @Test
    @DisplayName("Service without config should be return default yaml spec")
    void serviceWithDefaultYamlSpec() throws InterruptedException, ExecutionException, TimeoutException {
        check(
            new Test2ServiceImpl(ConfigFactory.load()),
            yamlToJson(resourceAsString("test2.yaml"))
        );
    }

    @Test
    @DisplayName("Service without config should be return default yml spec")
    void serviceWithDefaultYmlSpec() throws InterruptedException, ExecutionException, TimeoutException {
        check(
            new Test3ServiceImpl(ConfigFactory.load()),
            yamlToJson(resourceAsString("test3.yml"))
        );
    }

}
