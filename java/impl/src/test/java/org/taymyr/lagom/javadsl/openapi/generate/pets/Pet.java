package org.taymyr.lagom.javadsl.openapi.generate.pets;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Value
public class Pet {
    @Schema(required = true)
    Long id;
    @Schema(required = true)
    String name;
    String tag;
}
