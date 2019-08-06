package com.learnwiremock.exception;

public class MovieErrorResponse extends  RuntimeException {

    public MovieErrorResponse(String message, Throwable cause) {
        super(message, cause);
    }
    public MovieErrorResponse(Throwable cause) {
        super(cause);
    }
}
