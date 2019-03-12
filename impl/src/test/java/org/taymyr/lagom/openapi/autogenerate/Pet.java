package org.taymyr.lagom.openapi.autogenerate;

import io.swagger.v3.oas.annotations.media.Schema;

public class Pet {

    @Schema(required = true)
    private Integer id;
    @Schema(required = true)
    private String name;
    private String tag;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

}
