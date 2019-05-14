package org.taymyr.lagom.openapi.autogenerate.pets;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Value
@Schema(requiredProperties = {"code", "message"})
public class Error {
    private Integer code;
    private String message;
}
