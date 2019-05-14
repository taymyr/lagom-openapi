package org.taymyr.lagom.openapi.autogenerate.pets;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Value
public class Pet {
    @Schema(required = true)
    private Integer id;
    @Schema(required = true)
    private String name;
    private String tag;
}
