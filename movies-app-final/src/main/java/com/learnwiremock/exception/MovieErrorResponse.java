package com.learnwiremock.exception;

import org.springframework.web.reactive.function.client.WebClientResponseException;

public class MovieErrorResponse extends RuntimeException {
    public MovieErrorResponse(String statusText, WebClientResponseException ex) {
        super(statusText,ex);
    }

    public MovieErrorResponse(Exception ex) {
        super(ex);
    }
}
