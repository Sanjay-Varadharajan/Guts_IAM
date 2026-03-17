package com.guts.Guts_IAM.exceptionhandling.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private String errorCode;

    private HttpStatus status;

    public ApiException(String message,String errorCode,HttpStatus status) {
        super(message);
        this.errorCode=errorCode;
        this.status=status;
    }
}
