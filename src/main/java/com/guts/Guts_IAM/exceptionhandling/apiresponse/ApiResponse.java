package com.guts.Guts_IAM.exceptionhandling.apiresponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean isResponseSuccess;

    private String responseMessage;

    private T responseBody;

    private LocalDateTime responseOn;

}
