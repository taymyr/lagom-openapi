package org.taymyr.lagom.internal.openapi;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taymyr.lagom.internal.openapi.cases.ArrayParameterService;
import org.taymyr.lagom.internal.openapi.cases.ClassHiddenService;
import org.taymyr.lagom.internal.openapi.cases.LinksService;
import org.taymyr.lagom.internal.openapi.cases.OpenAPIDefinitionService;
import org.taymyr.lagom.internal.openapi.cases.ParameterService;
import org.taymyr.lagom.internal.openapi.cases.RefSecurityService;
import org.taymyr.lagom.internal.openapi.cases.RequestBodyService;
import org.taymyr.lagom.internal.openapi.cases.SecurityService;
import org.taymyr.lagom.internal.openapi.cases.ServersService;
import org.taymyr.lagom.internal.openapi.cases.TagsService;

import java.util.List;

import static org.assertj.core.util.Lists.emptyList;
import static org.taymyr.lagom.internal.openapi.TestUtils.checkOpenAPISpec;

class OpenAPITest {

    @Test
    @DisplayName("Test service with annotation OpenAPIDefinition")
    void testOpenAPIDefinitionService() {
        checkOpenAPISpec(
            new LagomServiceInfo(OpenAPIDefinitionService.class, emptyList()),
            "OpenAPIDefinitionService.yml"
        );
    }

    @Test
    @DisplayName("Test service with class annotation Hidden")
    void testClassHiddenService() {
        checkOpenAPISpec(
            new LagomServiceInfo(ClassHiddenService.class, emptyList()),
            "ClassHiddenService.yml"
        );
    }

    @Test
    @DisplayName("Test service with parameters")
    void testParameterService() throws NoSuchMethodException {
        checkOpenAPISpec(
            new LagomServiceInfo(ParameterService.class, ImmutableList.of(
                new LagomCallInfo(ParameterService.class.getDeclaredMethod("test", String.class), "/test/{id}", "GET")
            )),
            "ParameterService.yml"
        );
    }

    @Test
    @DisplayName("Test service with array parameters")
    void testArrayParameterService() throws NoSuchMethodException {
        checkOpenAPISpec(
            new LagomServiceInfo(ArrayParameterService.class, ImmutableList.of(
                new LagomCallInfo(ArrayParameterService.class.getDeclaredMethod("test", List.class), "/test", "GET")
            )),
            "ArrayParameterService.yml"
        );
    }

    @Test
    @DisplayName("Test service with links")
    void testLinksService() throws NoSuchMethodException {
        checkOpenAPISpec(
            new LagomServiceInfo(LinksService.class, ImmutableList.of(
                new LagomCallInfo(LinksService.class.getDeclaredMethod("test", String.class), "/test/{userId}", "GET")
            )),
            "LinksService.yml"
        );
    }

    @Test
    @DisplayName("Test service with request body")
    void testRequestBodyService() throws NoSuchMethodException {
        checkOpenAPISpec(
            new LagomServiceInfo(RequestBodyService.class, ImmutableList.of(
                new LagomCallInfo(RequestBodyService.class.getDeclaredMethod("create"), "/create", "POST")
            )),
            "RequestBodyService.yml"
        );
    }

    @Test
    @DisplayName("Test service with servers")
    void testServersService() throws NoSuchMethodException {
        checkOpenAPISpec(
            new LagomServiceInfo(ServersService.class, ImmutableList.of(
                new LagomCallInfo(ServersService.class.getDeclaredMethod("test"), "/test", "GET")
            )),
            "ServersService.yml"
        );
    }

    @Test
    @DisplayName("Test service with tags")
    void testServiceWithTags() throws NoSuchMethodException {
        checkOpenAPISpec(
            new LagomServiceInfo(TagsService.class, ImmutableList.of(
                new LagomCallInfo(TagsService.class.getDeclaredMethod("test"), "/", "GET")
            )),
            "TagsService.yml"
        );
    }

    @Test
    @DisplayName("Test service with securities")
    void testSecurityService() throws NoSuchMethodException {
        checkOpenAPISpec(
            new LagomServiceInfo(SecurityService.class, ImmutableList.of(
                new LagomCallInfo(SecurityService.class.getDeclaredMethod("test1"), "/test1", "GET"),
                new LagomCallInfo(SecurityService.class.getDeclaredMethod("test2"), "/test2", "GET")
            )),
            "SecurityService.yml"
        );
    }

    @Test
    @DisplayName("Test service with ref security")
    void testRefSecurityService() throws NoSuchMethodException {
        checkOpenAPISpec(
            new LagomServiceInfo(RefSecurityService.class, ImmutableList.of(
                new LagomCallInfo(RefSecurityService.class.getDeclaredMethod("secured"), "/cases", "GET")
            )),
            "RefSecurityService.yml"
        );
    }
}

