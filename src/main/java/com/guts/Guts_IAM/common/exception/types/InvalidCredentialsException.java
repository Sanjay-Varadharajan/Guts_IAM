package com.guts.Guts_IAM.common.exception.types;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends RuntimeException {
    private String errorCode;

    private HttpStatus status;


    public InvalidCredentialsException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
}
