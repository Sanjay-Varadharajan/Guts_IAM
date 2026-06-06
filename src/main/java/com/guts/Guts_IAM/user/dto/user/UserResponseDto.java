package com.guts.Guts_IAM.user.dto.user;

import com.guts.Guts_IAM.role.model.Role;
import com.guts.Guts_IAM.user.model.User;
import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {

    private Integer userId;

    private String userName;

    private String userMail;

    private boolean active;

    private Set<String> roles;

    private boolean emailVerified;

    private LocalDateTime userCreatedOn;

    private boolean accountNonLocked;


    public UserResponseDto(User user) {
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.userMail = user.getUserMail();
        this.active = user.isActive();
        this.roles=user.getRoles().stream()
                .map(roles->roles.getName())
                .collect(Collectors.toSet());
        this.accountNonLocked=user.isAccountNonLocked();
        this.emailVerified=user.isEmailVerified();
        this.userCreatedOn=user.getUserCreatedOn();
    }
}
