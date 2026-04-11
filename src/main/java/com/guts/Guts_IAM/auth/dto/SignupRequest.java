package com.guts.Guts_IAM.auth.dto;


import com.guts.Guts_IAM.user.model.User;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {

    @NotEmpty(message = "userName must be filled")
    private String userName;

    @NotEmpty(message = "mail is required for this action")
    @Column(nullable = false,unique = true)
    private String userMail;

    @NotEmpty(message = "password is required")
    private String userPassword;

    public SignupRequest(User signedUpUser) {
        this.userMail=signedUpUser.getUserMail();
        this.userName=signedUpUser.getUserName();
        this.userPassword= signedUpUser.getUserPassword();
    }
}
