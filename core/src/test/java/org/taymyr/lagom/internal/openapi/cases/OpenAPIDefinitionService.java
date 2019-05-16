package org.taymyr.lagom.internal.openapi.cases;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@OpenAPIDefinition(
    info = @Info(
        version = "1.0.0",
        title = "Test",
        description = "Description",
        contact = @Contact(
            name = "Name",
            email = "mail@example.com",
            url = "https://example.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    )
)
public interface OpenAPIDefinitionService extends Service {
    @Override
    default Descriptor descriptor() {
        return Service.named("test");
    }
}
