package com.guts.Guts_IAM.common.exception.types;

import org.springframework.http.HttpStatus;

public class ApiKeyNotFoundException extends RuntimeException {

    private String errorCode;

    private HttpStatus status;


    public ApiKeyNotFoundException(String message,String errorCode,HttpStatus status) {
        super(message);
        this.errorCode=errorCode;
        this.status=status;
    }
}
