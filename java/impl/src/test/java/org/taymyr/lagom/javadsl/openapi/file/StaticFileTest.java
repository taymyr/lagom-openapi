package org.taymyr.lagom.javadsl.openapi.file;

import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taymyr.lagom.javadsl.openapi.AbstractOpenAPIServiceKt;

import static com.lightbend.lagom.javadsl.api.transport.ResponseHeader.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.taymyr.lagom.internal.openapi.TestUtils.resourceAsString;

class StaticFileTest {

    private ResponseHeader OK_JSON = OK.withProtocol(AbstractOpenAPIServiceKt.getJSON());

    @Test
    @DisplayName("Service with incorrect config should be return 404")
    void shouldReturn404() {
        Config config = ConfigFactory.parseString("openapi.file = file");
        Test1ServiceImpl service = new Test1ServiceImpl(config);
        assertThatExceptionOfType(NotFound.class)
            .isThrownBy(() -> service.openapi().invokeWithHeaders(null, null))
            .withMessage("OpenAPI specification not found")
            .has(new Condition<>(thr -> thr.errorCode().http() == 404, "Error code must be 404"));
    }

    @Test
    @DisplayName("Service with correct config should be return spec")
    void serviceWithCorrectConfig() {
        String originalSpec = resourceAsString("config.json");
        Config config = ConfigFactory.parseString("openapi.file = config.json");
        Test1ServiceImpl service = new Test1ServiceImpl(config);
        assertThat(service.openapi().invokeWithHeaders(null, null))
            .isCompleted()
            .isCompletedWithValueMatching(pair -> pair.first().equals(OK_JSON), "Must be equal to OK_JSON")
            .isCompletedWithValueMatching(pair -> pair.second().equals(originalSpec), "Must be equal to originalSpec")
            .isDone();
    }

    @Test
    @DisplayName("Service without config should be return default json spec")
    void serviceWithDefaultJsonSpec() {
        String originalSpec = resourceAsString("test1.json");
        Test1ServiceImpl service = new Test1ServiceImpl(ConfigFactory.load());
        assertThat(service.openapi().invokeWithHeaders(null, null))
            .isCompleted()
            .isCompletedWithValueMatching(pair -> pair.first().equals(OK_JSON), "Must be equal to OK_JSON")
            .isCompletedWithValueMatching(pair -> pair.second().equals(originalSpec), "Must be equal to originalSpec")
            .isDone();
    }

    @Test
    @DisplayName("Service without config should be return default yaml spec")
    void serviceWithDefaultYamlSpec() {
        String originalSpec = resourceAsString("test2.yaml");
        Test2ServiceImpl service = new Test2ServiceImpl(ConfigFactory.load());
        assertThat(service.openapi().invokeWithHeaders(null, null))
            .isCompleted()
            .isCompletedWithValueMatching(pair -> pair.first().status() == 200, "Status code must be 200")
            .isCompletedWithValueMatching(pair -> pair.first().protocol().equals(AbstractOpenAPIServiceKt.getYAML()), "Protocol must be YAML")
            .isCompletedWithValueMatching(pair -> pair.second().equals(originalSpec), "Must be equal to originalSpec")
            .isDone();
    }

    @Test
    @DisplayName("Service without config should be return default yml spec")
    void serviceWithDefaultYmlSpec() {
        String originalSpec = resourceAsString("test3.yml");
        Test3ServiceImpl service = new Test3ServiceImpl(ConfigFactory.load());
        Assertions.assertThat(service.openapi().invokeWithHeaders(null, null))
            .isCompleted()
            .isCompletedWithValueMatching(pair -> pair.first().status() == 200, "Status code must be 200")
            .isCompletedWithValueMatching(pair -> pair.first().protocol().equals(AbstractOpenAPIServiceKt.getYAML()), "Protocol must be YAML")
            .isCompletedWithValueMatching(pair -> pair.second().equals(originalSpec), "Must be equal to originalSpec")
            .isDone();
    }

}
