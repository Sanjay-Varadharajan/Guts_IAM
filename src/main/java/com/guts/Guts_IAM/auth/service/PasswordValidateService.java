package com.guts.Guts_IAM.auth.service;


import com.guts.Guts_IAM.auth.properties.PasswordPolicyProperties;
import com.guts.Guts_IAM.auth.properties.PasswordValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordValidateService {

    private final BreachedPasswordService breachedPasswordService;
    private final PasswordPolicyProperties passwordPolicyProperties;

    public PasswordValidationResult validationResult(String password){
        if (password==null || password.isBlank()){
            return new PasswordValidationResult(
                    false,
                    "Password Cannot be Empty"
            );
        }

        if (password.length() < passwordPolicyProperties.getMinLength()) {

            return new PasswordValidationResult(
                    false,
                    "Password must be at least "
                            + passwordPolicyProperties.getMinLength()
                            + " characters long"
            );
        }

        if (passwordPolicyProperties.isRequireUppercase()
                && !password.matches(".*[A-Z].*")) {

            return new PasswordValidationResult(
                    false,
                    "Password must contain at least one uppercase letter"
            );
        }
        if (passwordPolicyProperties.isRequireLowercase()
                && !password.matches(".*[a-z].*")) {

            return new PasswordValidationResult(
                    false,
                    "Password must contain at least one lowercase letter"
            );
        }

        if (passwordPolicyProperties.isRequireNumber()
                && !password.matches(".*\\d.*")) {

            return new PasswordValidationResult(
                    false,
                    "Password must contain at least one number"
            );
        }

        if (passwordPolicyProperties.isRequireSpecialCharacter()
                && !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {

            return new PasswordValidationResult(
                    false,
                    "Password must contain at least one special character"
            );
        }

        if (passwordPolicyProperties.isCheckBreachedPasswords()
                && breachedPasswordService.isBreached(password)) {

            return new PasswordValidationResult(
                    false,
                    "This password has appeared in known data breaches"
            );
        }

        return new PasswordValidationResult(
                true,
                "Password is valid"
        );
    }
}
