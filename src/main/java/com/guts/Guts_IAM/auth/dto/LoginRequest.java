package com.guts.Guts_IAM.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {


    @NotEmpty(message = "This field cannot be Empty")
    private String userMail;

    @NotEmpty(message = "Password is Required")
    private String userPassword;


}
