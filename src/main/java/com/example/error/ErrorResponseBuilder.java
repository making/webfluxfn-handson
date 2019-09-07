package com.example.error;

import am.ik.yavi.core.ConstraintViolations;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ErrorResponseBuilder {

    private Map<String, List<String>> details;

    private String error;

    private String message;

    private int status;

    public ErrorResponse build() {
        return new ErrorResponse(status, error, message, details);
    }

    public ErrorResponseBuilder withDetails(Map<String, List<String>> details) {
        this.details = details;
        return this;
    }

    public ErrorResponseBuilder withDetails(ConstraintViolations violations) {
        MultiValueMap<String, String> details = new LinkedMultiValueMap<>();
        violations.details().forEach(d -> details.add((String) d.getArgs()[0], d.getDefaultMessage()));
        this.details = Collections.unmodifiableMap(details);
        return this;
    }

    public ErrorResponseBuilder withMessage(String message) {
        this.message = message;
        return this;
    }

    public ErrorResponseBuilder withStatus(HttpStatus status) {
        this.status = status.value();
        this.error = status.getReasonPhrase();
        return this;
    }
}