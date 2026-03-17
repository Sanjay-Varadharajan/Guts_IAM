package com.guts.Guts_IAM.security.signup;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDto {


    @NotEmpty(message = "This field cannot be Empty")
    private String userMail;

    @NotEmpty(message = "Password is Required")
    private String userPassword;
}
