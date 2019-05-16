package org.taymyr.lagom.javadsl.openapi.file;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import org.taymyr.lagom.javadsl.openapi.OpenAPIService;
import org.taymyr.lagom.javadsl.openapi.OpenAPIService;

public interface Test1Service extends OpenAPIService {
    @Override
    default Descriptor descriptor() {
        return withOpenAPI(Service.named("test1"));
    }
}
