package com.guts.Guts_IAM.security.signup;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {

    private String accessToken;

    private String refreshToken;

    private String tokenType= "Bearer ";


}
