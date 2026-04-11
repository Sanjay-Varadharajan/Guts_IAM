package com.guts.Guts_IAM.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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
