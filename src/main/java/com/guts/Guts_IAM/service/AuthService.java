package com.guts.Guts_IAM.service;

import com.guts.Guts_IAM.auth.dto.LoginRequest;
import com.guts.Guts_IAM.common.exception.types.ResourceNotFoundException;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

    public JwtResponse login(LoginRequest loginRequest, HttpServletRequest request) {

        User user = userRepository.findByUserMailAndActiveTrue(loginRequest.getUserMail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isAccountNonLocked()) {
            throw new RuntimeException("Account is locked. Please unlock using OTP.");
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
                throw new RuntimeException(
                        "Invalid credentials. Attempt " + attempts + "/3"
                );
            }
        }

        User loggedInUser = userRepository.findByUserMailAndActiveTrue(loginRequest.getUserMail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtUtils.generateAccessToken(loggedInUser);
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
        auditLog.setUserAgent(request.getHeader("User-Agent"));
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
        auditLog.setUserAgent(httpServletRequest.getHeader("User-Agent"));

        auditRepo.save(auditLog);
        refreshTokenRepository.deleteByToken(refreshTokenStr);
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
    }