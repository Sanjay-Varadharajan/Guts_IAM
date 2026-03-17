package com.guts.Guts_IAM.exceptionhandling.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ForbiddenException extends RuntimeException {

    private String errorCode;

    private HttpStatus status;


    public ForbiddenException(String message,String errorCode,HttpStatus httpStatus){
        super(message);
        this.errorCode=errorCode;
        this.status=httpStatus;
    }


}
