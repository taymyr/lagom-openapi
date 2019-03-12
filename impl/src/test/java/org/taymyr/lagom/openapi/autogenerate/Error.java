package org.taymyr.lagom.openapi.autogenerate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(requiredProperties = {"code", "message"})
public class Error {

    private Integer code;
    private String message;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
