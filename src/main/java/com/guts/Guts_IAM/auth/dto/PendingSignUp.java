package com.guts.Guts_IAM.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PendingSignUp implements Serializable {


    private String userName;

    private String userMail;

    private String userPassword;
    
}
