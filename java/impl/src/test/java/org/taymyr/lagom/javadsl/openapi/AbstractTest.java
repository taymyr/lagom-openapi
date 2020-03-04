package org.taymyr.lagom.javadsl.openapi;

import akka.japi.Pair;
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.lightbend.lagom.javadsl.api.transport.ResponseHeader.OK;
import static java.util.Optional.empty;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.taymyr.lagom.internal.openapi.TestUtils.eventually;
import static org.taymyr.lagom.internal.openapi.Utils.yamlToJson;

@SuppressWarnings("KotlinInternalInJava") // https://youtrack.jetbrains.com/issue/KT-35518
public abstract class AbstractTest {

    protected static final ResponseHeader OK_JSON = OK.withProtocol(AbstractOpenAPIServiceKt.getJSON());
    protected static final ResponseHeader OK_YAML = OK.withProtocol(AbstractOpenAPIServiceKt.getYAML());

    protected void check(AbstractOpenAPIService service, String expectedJson) throws InterruptedException, ExecutionException, TimeoutException {
        Pair<ResponseHeader, String> openapi = eventually(service.openapi(Optional.of("JsOn")).invokeWithHeaders(null, null));
        assertThat(openapi.first()).isEqualTo(OK_JSON);
        assertThatJson(openapi.second()).when(IGNORING_ARRAY_ORDER).isEqualTo(expectedJson);

        openapi = eventually(service.openapi(empty()).invokeWithHeaders(null, null));
        assertThat(openapi.first()).isEqualTo(OK_YAML);
        assertThatJson(yamlToJson(openapi.second())).when(IGNORING_ARRAY_ORDER).isEqualTo(expectedJson);
    }

}
