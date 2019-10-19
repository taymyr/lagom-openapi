package org.taymyr.lagom.internal.openapi;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.swagger.v3.core.util.Yaml;
import net.javacrumbs.jsonunit.core.Option;

import java.io.IOException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.taymyr.lagom.internal.openapi.Utils.yamlToJson;

public final class TestUtils {

    private TestUtils() {
    }

    @SuppressWarnings("UnstableApiUsage")
    public static String resourceAsString(String resourceName) {
        try {
            return Resources.toString(Resources.getResource(resourceName), Charsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    public static <T> T eventually(CompletionStage<T> stage) throws InterruptedException, ExecutionException, TimeoutException {
        return stage.toCompletableFuture().get(5, SECONDS);
    }

    static void checkOpenAPISpec(LagomServiceInfo serviceInfo, String expectedYamlPath) {
        String expected = yamlToJson(resourceAsString(expectedYamlPath));
        String actual = Yaml.pretty(new SpecGenerator().generate(serviceInfo));
        assertThatJson(yamlToJson(actual)).when(Option.IGNORING_ARRAY_ORDER).isEqualTo(expected);
    }

}
