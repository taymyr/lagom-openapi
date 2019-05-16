package org.taymyr.lagom.internal.openapi.cases;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(example = "{\"id\" : 123, \"firstName\": \"Ivan\", \"secondName\": \"Petrov\", \"status\": 1}")
public class User {
    private long id;
    private String firstName;
    private String lastName;
    @Schema(description = "User Status", allowableValues = {"1","2","3"})
    private int status;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}