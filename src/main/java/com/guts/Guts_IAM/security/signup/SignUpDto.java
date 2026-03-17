package com.guts.Guts_IAM.security.signup;


import com.guts.Guts_IAM.model.User;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatusCode;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpDto {

    @NotEmpty(message = "userName must be filled")
    private String userName;

    @NotEmpty(message = "mail is required for this action")
    @Column(nullable = false,unique = true)
    private String userMail;

    @NotEmpty(message = "password is required")
    private String userPassword;

    public SignUpDto(User signedUpUser) {
        this.userMail=signedUpUser.getUserMail();
        this.userName=signedUpUser.getUserName();
        this.userPassword= signedUpUser.getUserPassword();
    }
}
