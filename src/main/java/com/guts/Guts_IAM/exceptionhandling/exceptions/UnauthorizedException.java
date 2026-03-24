package com.guts.Guts_IAM.exceptionhandling.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends RuntimeException {

    private String errorCode;

    private HttpStatus errorStatus;

    private String jwtTokenExpired;

    public UnauthorizedException(String message,String errorCode,HttpStatus errorStatus) {
        super(message);
        this.errorCode=errorCode;
        this.errorStatus=errorStatus;
    }

    public UnauthorizedException(String jwtTokenExpired) {
        this.jwtTokenExpired=jwtTokenExpired;
    }
}
