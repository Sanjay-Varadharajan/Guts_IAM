package com.guts.Guts_IAM.auth.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@Configuration
public class PasswordPolicyProperties {

    private int minLength;

    private boolean requireUppercase;

    private boolean requireLowercase;

    private boolean requireNumber;

    private boolean requireSpecialCharacter;

    private boolean checkBreachedPasswords;
}
