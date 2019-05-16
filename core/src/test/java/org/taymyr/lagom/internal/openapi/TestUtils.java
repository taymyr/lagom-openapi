package org.taymyr.lagom.internal.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.swagger.v3.core.util.Yaml;
import net.javacrumbs.jsonunit.core.Option;

import java.io.IOException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import static java.util.concurrent.TimeUnit.SECONDS;

public class TestUtils {

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

    public static String yamlToJson(String yaml) {
        try {
            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
            Object obj = yamlReader.readValue(yaml, Object.class);
            ObjectMapper jsonWriter = new ObjectMapper();
            return jsonWriter.writeValueAsString(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void checkOpenAPISpec(LagomServiceInfo serviceInfo, String expectedYamlPath) {
        String expected = yamlToJson(resourceAsString(expectedYamlPath));
        String actual = Yaml.pretty(new SpecGenerator().generate(serviceInfo));
        assertThatJson(yamlToJson(actual)).when(Option.IGNORING_ARRAY_ORDER).isEqualTo(expected);
    }

}
