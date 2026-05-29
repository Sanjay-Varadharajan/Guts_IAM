package com.guts.Guts_IAM.auth.service;

import com.guts.Guts_IAM.auditlog.action.Action;
import com.guts.Guts_IAM.auditlog.action.AuditStatus;
import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
import com.guts.Guts_IAM.auditlog.service.AuditLogService;
import com.guts.Guts_IAM.auth.dto.PendingSignUp;
import com.guts.Guts_IAM.auth.dto.SignupRequest;
import com.guts.Guts_IAM.common.exception.types.ConflictException;
import com.guts.Guts_IAM.common.exception.types.ResourceNotFoundException;
import com.guts.Guts_IAM.common.mail.EmailService;
import com.guts.Guts_IAM.common.response.ApiResponse;
import com.guts.Guts_IAM.role.model.Role;
import com.guts.Guts_IAM.role.repository.RoleRepository;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final AuditLogService auditLogService;


    private final RoleRepository roleRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private final EmailService emailService;

    public ApiResponse signup(
            SignupRequest signUpRequest,
            HttpServletRequest httpServletRequest
    ) {

        Optional<User> userExists =
                userRepository.findByUserMailAndActiveTrue(
                        signUpRequest.getUserMail()
                );

        if (userExists.isPresent()) {

            auditLogService.log(
                    userExists.get(),
                    Action.SIGNUP,
                    "AUTH",
                    userExists.get().getUserId().toString(),
                    AuditStatus.FAILED,
                    "Signup attempted with already registered email",
                    httpServletRequest
            );


            throw new ConflictException(
                    "User Already Exists",
                    "USER_EXISTS",
                    HttpStatus.CONFLICT
            );
        }

        String verificationToken =
                UUID.randomUUID().toString();

        PendingSignUp pendingSignup =
                new PendingSignUp();

        pendingSignup.setUserName(
                signUpRequest.getUserName()
        );

        pendingSignup.setUserMail(
                signUpRequest.getUserMail()
        );

        pendingSignup.setUserPassword(
                bCryptPasswordEncoder.encode(
                        signUpRequest.getUserPassword()
                )
        );

        redisTemplate.opsForValue().set(
                "signup:" + verificationToken,
                pendingSignup,
                Duration.ofMinutes(15)
        );

        emailService.sendVerificationEmail(
                pendingSignup.getUserMail(),
                verificationToken
        );
        auditLogService.log(
                null,
                Action.SEND_VERIFICATION_EMAIL,
                "AUTH",
                pendingSignup.getUserMail(),
                AuditStatus.SUCCESS,
                "Verification email sent successfully",
                httpServletRequest
        );



        return new ApiResponse(
                true,
                "Verification email sent successfully",
                null,
                LocalDateTime.now()
        );
    }

    public ApiResponse verifyEmail(
            String token,
            HttpServletRequest httpServletRequest
    ) {




        PendingSignUp pendingSignup =
                (PendingSignUp) redisTemplate
                        .opsForValue()
                        .get("signup:" + token);



        if (pendingSignup == null) {
            auditLogService.log(
                    null,
                    Action.VERIFY_EMAIL,
                    "AUTH",
                   null ,
                    AuditStatus.FAILED,
                    "Invalid or expired email verification token used",
                    httpServletRequest
            );

            throw new ConflictException(
                    "Invalid or expired token",
                    "TOKEN_INVALID",
                    HttpStatus.BAD_REQUEST
            );
        }


        Optional<User> existingUser =
                userRepository.findByUserMailAndActiveTrue(
                        pendingSignup.getUserMail()
                );

        if (existingUser.isPresent()) {
            auditLogService.log(
                    existingUser.get(),
                    Action.VERIFY_EMAIL,
                    "AUTH",
                    existingUser.get().getUserId().toString(),
                    AuditStatus.FAILED,
                    "Email verification attempted for already existing user",
                    httpServletRequest
            );

            throw new ConflictException(
                    "User Already Exists",
                    "USER_EXISTS",
                    HttpStatus.CONFLICT
            );
        }

        Role userRole =
                roleRepository.findByName("ROLE_USER")
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Role not found",
                                        "NOT_FOUND",
                                        HttpStatus.NOT_FOUND
                                )
                        );

        Set<Role> roles = new HashSet<>();

        roles.add(userRole);

        User user = new User();

        user.setUserName(
                pendingSignup.getUserName()
        );

        user.setUserMail(
                pendingSignup.getUserMail()
        );

        user.setUserPassword(
                pendingSignup.getUserPassword()
        );

        user.setRoles(roles);

        user.setActive(true);

        user.setEmailVerified(true);

        user.setVerificationToken(
                UUID.randomUUID().toString()
        );

        User savedUser =
                userRepository.save(user);

        auditLogService.log(
                savedUser,
                Action.VERIFY_EMAIL,
                "AUTH",
                savedUser.getUserId().toString(),
                AuditStatus.SUCCESS,
                "Email verified successfully and account created",
                httpServletRequest
        );

        redisTemplate.delete(
                "signup:" + token
        );


        return new ApiResponse(
                true,
                "Email verified successfully",
                null,
                LocalDateTime.now()
        );
    }
}