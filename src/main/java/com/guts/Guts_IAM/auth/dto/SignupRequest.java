package com.guts.Guts_IAM.auth.dto;


import com.guts.Guts_IAM.user.model.User;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {

    @NotBlank(message = "userName must be filled")
    private String userName;

    @NotBlank(message = "mail is required for this action")
    @Column(nullable = false,unique = true)
    private String userMail;

    @NotBlank(message = "password is required")
    private String userPassword;

    public SignupRequest(User signedUpUser) {
        this.userMail=signedUpUser.getUserMail();
        this.userName=signedUpUser.getUserName();
        this.userPassword= signedUpUser.getUserPassword();
    }
}
