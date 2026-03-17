package com.guts.Guts_IAM.exceptionhandling.apierrorresponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
public class ApiErrorResponse {
    private String message;

    private HttpStatus statusCode;

    private String errorCode;

    private Map<String,String> error;

    private LocalDateTime errorOccurredOn;

    private String path;
}
