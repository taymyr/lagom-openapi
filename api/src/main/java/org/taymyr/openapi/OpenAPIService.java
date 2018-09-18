package org.taymyr.openapi;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import static com.lightbend.lagom.javadsl.api.Service.pathCall;

import static java.lang.String.format;

/**
 * OpenAPI service descriptor.
 *
 * @author Sergey Morgunov
 */
public interface OpenAPIService extends Service {

    /**
     * Return OpenAPI specification for current service.
     * @return OpenAPI specification
     */
    ServiceCall<NotUsed, String> openapi();

    default Descriptor withOpenAPI(Descriptor descriptor) {
        return descriptor.withCalls(
                pathCall(format("/%s/openapi", descriptor.name()), this::openapi)
        );
    }

}
