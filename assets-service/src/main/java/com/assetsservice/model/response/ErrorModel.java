package com.assetsservice.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
@Getter
public class ErrorModel {

    private String error;
    private String code;

    public ErrorModel() {
    }

    public ErrorModel(String error, String code) {
        this.error = error;
        this.code = code;
    }
}
