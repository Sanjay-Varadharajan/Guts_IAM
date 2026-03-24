package com.guts.Guts_IAM.security.signup;

import com.guts.Guts_IAM.model.user.User;
import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {

    private Integer userId;

    private String userName;

    private String userMail;

    private boolean active;

    public UserResponseDto(User user) {
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.userMail = user.getUserMail();
        this.active = user.isActive();
    }
}
