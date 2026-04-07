package com.guts.Guts_IAM.security.signup;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class UserRequestDto {
    private String userName;
}
