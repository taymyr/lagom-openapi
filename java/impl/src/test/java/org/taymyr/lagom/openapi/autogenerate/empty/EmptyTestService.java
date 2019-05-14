package org.taymyr.lagom.openapi.autogenerate.empty;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import org.taymyr.lagom.openapi.OpenAPIService;

public interface EmptyTestService extends OpenAPIService  {
    @Override
    default Descriptor descriptor() {
        return withOpenAPI(Service.named("test"));
    }
}
