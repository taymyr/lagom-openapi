package org.taymyr.lagom.openapi.file;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import org.taymyr.lagom.openapi.OpenAPIService;

public interface Test2Service extends OpenAPIService {
    @Override
    default Descriptor descriptor() {
        return withOpenAPI(Service.named("test2"));
    }
}
