package com.guts.Guts_IAM.common.exception.types;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResourceNotFoundException extends RuntimeException {


    private String errorCode;

    private HttpStatus status;


    public ResourceNotFoundException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
}
