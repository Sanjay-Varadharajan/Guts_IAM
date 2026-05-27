package com.guts.Guts_IAM.auth.service;

import com.guts.Guts_IAM.auth.dto.LoginRequest;
import com.guts.Guts_IAM.common.exception.types.AccountLockedException;
import com.guts.Guts_IAM.common.exception.types.ResourceNotFoundException;
import com.guts.Guts_IAM.security.userdetails.CustomUserDetails;
import com.guts.Guts_IAM.sessionmanagement.model.UserSession;
import com.guts.Guts_IAM.sessionmanagement.repository.UserSessionRepository;
import com.guts.Guts_IAM.sessionmanagement.service.SessionService;
import com.guts.Guts_IAM.token.audit.TokenAudit;
import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.token.refreshtoken.model.RefreshToken;
import com.guts.Guts_IAM.role.model.Role;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
import com.guts.Guts_IAM.token.audit.TokenAuditRepository;
import com.guts.Guts_IAM.token.refreshtoken.repository.RefreshTokenRepository;
import com.guts.Guts_IAM.user.repository.UserRepository;
import com.guts.Guts_IAM.security.jwt.util.JwtUtils;
import com.guts.Guts_IAM.security.jwt.dto.JwtResponse;
import com.guts.Guts_IAM.security.util.hashutil.HashUtil;
import com.guts.Guts_IAM.auditlog.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenAuditRepository tokenAuditRepository;
    private final AuditRepository auditRepo;
    private final AuditService auditService;
    private final SessionService sessionService;
    private final UserSessionRepository userSessionRepository;

    public JwtResponse login(LoginRequest loginRequest, HttpServletRequest request) {

        User user = userRepository.findByUserMailAndActiveTrue(loginRequest.getUserMail())
                .orElseThrow(() -> new ResourceNotFoundException("user not found","NOT_FOUND",HttpStatus.NOT_FOUND));

        if (!user.isAccountNonLocked()) {
            throw new AccountLockedException("Account is locked. Please unlock using OTP.","ACCOUNT_LOCKED",HttpStatus.LOCKED);
        }

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUserMail(),
                            loginRequest.getUserPassword()
                    )
            );

            user.setFailedAttempts(0);
            userRepository.save(user);

        } catch (Exception e) {

            int attempts = updateFailedAttempts(user);

            if (attempts >= 3) {
                AuditLog auditLog=new AuditLog();
                auditLog.setLogAction("Account Locked due to multiple attempts to login");
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setIpAddress(request.getRemoteAddr());
                auditLog.setRoleName(user.getRoles().toString());
                auditLog.setResource("AUTH");
                auditLog.setUserMail(user.getUserMail());
                auditLog.setUserId(user.getUserId());
                auditLog.setResourceId(user.getUserId().toString());
                auditService.saveAudit(auditLog);

                throw new RuntimeException(
                        "Account locked after 3 failed attempts. Please unlock via email."
                );
            } else {
                throw new BadCredentialsException(
                        "Invalid credentials. Attempt " + attempts + "/3"
                );
            }
        }

        User loggedInUser = userRepository.findByUserMailAndActiveTrue(loginRequest.getUserMail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtUtils.generateAccessToken(loggedInUser);
        sessionService.createSession(user, accessToken, request);

        RefreshToken refreshToken = createRefreshToken(loggedInUser);

        TokenAudit tokenAudit = new TokenAudit();
        tokenAudit.setAccessToken(HashUtil.sha256(accessToken));
        tokenAudit.setAction("Token generated for " + loggedInUser.getUserMail());
        tokenAudit.setTokenOwner(loggedInUser.getUserMail());
        tokenAuditRepository.save(tokenAudit);

        AuditLog auditLog = new AuditLog();
        auditLog.setLogAction("LOGIN");
        auditLog.setUserMail(loggedInUser.getUserMail());
        auditLog.setResource("AUTH");
        auditLog.setResourceId(loggedInUser.getUserId().toString());
        auditLog.setRoleName(loggedInUser.getRoles().toString());
        auditLog.setUserId(loggedInUser.getUserId());
        auditLog.setIpAddress(request.getRemoteAddr());
        String userAgent =
                Optional.ofNullable(
                        request.getHeader("User-Agent")
                ).orElse("UNKNOWN");

        auditLog.setUserAgent(request.getHeader(userAgent));
        auditRepo.save(auditLog);

        return new JwtResponse(accessToken, refreshToken.getToken(), "Bearer");
    }


    public RefreshToken createRefreshToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Date.from(Instant.now().plusMillis(jwtUtils.getRefreshTokenExpiry())));
        return refreshTokenRepository.save(token);
    }





    public void logout(String refreshTokenStr, HttpServletRequest httpServletRequest) {
        Optional<RefreshToken>  refreshTokenCheck=refreshTokenRepository.findByToken(refreshTokenStr);


        if(refreshTokenCheck.isEmpty()){
            throw new ResourceNotFoundException(
                    "Refresh Token Not Found",
                    "RESOURCE_NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );
        }

        AuditLog auditLog=new AuditLog();
        String userMail=refreshTokenCheck
                .get()
                .getUser()
                .getUserMail();

        Set<Role> rolesSet=refreshTokenCheck
                .get()
                .getUser()
                .getRoles();

        auditLog.setRoleName(rolesSet.toString());
        auditLog.setLogAction("LOGOUT");
        auditLog.setUserMail(userMail);
        auditLog.setUserId(refreshTokenCheck.get().getUser().getUserId());
        auditLog.setResourceId(refreshTokenCheck.get().getUser().getUserId().toString());
        auditLog.setResource("AUTH");
        auditLog.setIpAddress(httpServletRequest.getRemoteAddr());
        String userAgent =
                Optional.ofNullable(
                        httpServletRequest.getHeader("User-Agent")
                ).orElse("UNKNOWN");
        auditLog.setUserAgent(httpServletRequest.getHeader(userAgent));

        auditRepo.save(auditLog);

        refreshTokenRepository.deleteByToken(refreshTokenStr);

        String authHeader=httpServletRequest.getHeader("Authorization");
        String accessToken = null;

        if(authHeader != null &&
                authHeader.startsWith("Bearer ")) {

            accessToken = authHeader.substring(7);
        }
        Optional<UserSession> optionalSession =
                userSessionRepository.findByJwtToken(accessToken);

        if(optionalSession.isPresent()) {

            UserSession session = optionalSession.get();

            session.setRevoked(true);

            userSessionRepository.save(session);
        }

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int updateFailedAttempts(User user) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);

        if (attempts >= 3) {
            user.setAccountNonLocked(false);
            user.setLockTime(java.time.LocalDateTime.now());
        }

        userRepository.save(user);
        return attempts;
    }


    public void logoutAll(Authentication authentication,
                          HttpServletRequest request) {

        CustomUserDetails userDetails =
                (CustomUserDetails)
                        authentication.getPrincipal();

        Integer userId =
                userDetails.getUserId();

        refreshTokenRepository.deleteByUser_UserId(userId);

        List<UserSession> sessions =
                userSessionRepository
                        .findByUserUserIdAndRevokedFalse(userId);

        sessions.forEach(session -> {
            session.setRevoked(true);
        });

        userSessionRepository.saveAll(sessions);

        AuditLog auditLog = new AuditLog();

        auditLog.setUserId(Math.toIntExact(userId));

        auditLog.setUserMail(userDetails.getUsername());

        auditLog.setLogAction("LOGOUT_ALL");

        auditLog.setResource("AUTH");

        auditLog.setResourceId(userId.toString());

        auditLog.setIpAddress(
                request.getRemoteAddr()
        );

        auditLog.setAuditedOn(LocalDateTime.now());

        String userAgent =
                Optional.ofNullable(
                        request.getHeader("User-Agent")
                ).orElse("UNKNOWN");

        auditLog.setUserAgent(userAgent);

        auditLog.setRoleName(
                userDetails.getAuthorities().toString()
        );

        auditRepo.save(auditLog);
    }
    }