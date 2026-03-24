package com.pokertrack.dto.request;

import javax.validation.constraints.NotBlank;

public class CreatePlayerRequest {
    @NotBlank
    private String name;
    private String alias;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
