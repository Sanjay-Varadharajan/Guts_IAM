package com.guts.Guts_IAM.exceptionhandling.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends RuntimeException {

    private String errorCode;

    private HttpStatus errorStatus;

    public UnauthorizedException(String message,String errorCode,HttpStatus errorStatus) {
        super(message);
        this.errorCode=errorCode;
        this.errorStatus=errorStatus;
    }

}
