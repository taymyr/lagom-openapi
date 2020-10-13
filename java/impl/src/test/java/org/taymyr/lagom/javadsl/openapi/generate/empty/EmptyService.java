package org.taymyr.lagom.javadsl.openapi.generate.empty;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import org.taymyr.lagom.javadsl.openapi.OpenAPIService;
import org.taymyr.lagom.javadsl.openapi.OpenAPIUtils;

public interface EmptyService extends OpenAPIService  {
    @Override
    default Descriptor descriptor() {
        return OpenAPIUtils.withOpenAPI(Service.named("test"));
    }
}
