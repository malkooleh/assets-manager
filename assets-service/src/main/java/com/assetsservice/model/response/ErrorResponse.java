package com.assetsservice.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

public class ErrorResponse {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ErrorModel> errors = new ArrayList<>();

    public ErrorResponse() {
    }

    public ErrorResponse(List<ErrorModel> errors) {
        this.errors = errors;
    }

    public ErrorResponse(ErrorModel error) {
        addError(error);
    }

    public void addError(ErrorModel error) {
        this.errors.add(error);
    }

    public List<ErrorModel> getErrors() {
        return this.errors;
    }
}
