package com.guts.Guts_IAM.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {


    @NotEmpty(message = "This field cannot be Empty")
    private String userMail;

    @NotEmpty(message = "Password is Required")
    private String userPassword;
}
