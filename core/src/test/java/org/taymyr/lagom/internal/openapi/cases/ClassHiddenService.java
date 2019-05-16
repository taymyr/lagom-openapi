package org.taymyr.lagom.internal.openapi.cases;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;

@OpenAPIDefinition
@Hidden
public interface ClassHiddenService extends Service {

    @Override
    default Descriptor descriptor() {
        return Service.named("test");
    }
}
