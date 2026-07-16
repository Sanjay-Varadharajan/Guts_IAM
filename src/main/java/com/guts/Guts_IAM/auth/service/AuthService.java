package com.guts.Guts_IAM.auth.service;

import com.guts.Guts_IAM.auditlog.action.Action;
import com.guts.Guts_IAM.auditlog.action.AuditStatus;
import com.guts.Guts_IAM.auditlog.service.AuditLogService;
import com.guts.Guts_IAM.auth.dto.LoginRequest;
import com.guts.Guts_IAM.common.exception.types.AccountLockedException;
import com.guts.Guts_IAM.common.exception.types.InvalidCredentialsException;
import com.guts.Guts_IAM.common.exception.types.ResourceNotFoundException;
import com.guts.Guts_IAM.common.exception.types.TokenNotFoundException;
import com.guts.Guts_IAM.common.mail.EmailService;
import com.guts.Guts_IAM.geolocation.dto.GeoLocation;
import com.guts.Guts_IAM.geolocation.service.GeoIPService;
import com.guts.Guts_IAM.redis.service.RefreshTokenCacheService;
import com.guts.Guts_IAM.risk.result.RiskAnalysisResult;
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
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
    private final AuditLogService auditLogService;
    private final SessionService sessionService;
    private final UserSessionRepository userSessionRepository;
    private final RefreshTokenCacheService refreshTokenCacheService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final GeoIPService geoIPService;
    private final UserAgentAnalyzer userAgentAnalyzer;
    private final RedisTemplate redisTemplate;


    private static final String DUMMY_HASH ="$2a$12$wN1QnW6yw80KH.XGlDavKuQPAPW7i10O1jyXTxlMP8.XPeR2GKLle";


    public JwtResponse login(LoginRequest loginRequest, HttpServletRequest request) {

        Optional<User> optionalUser =
                userRepository.findByUserMailAndActiveTrue(
                        loginRequest.getUserMail()
                );


        if(optionalUser.isEmpty()) {
            passwordEncoder.matches(
                    loginRequest.getUserPassword(),
                    DUMMY_HASH
            );
            auditLogService.log(
                    null,
                    Action.LOGIN,
                    "AUTH",
                    "UNKNOWN",
                    AuditStatus.FAILED,
                    "Login attempted with unknown email: "
                            + loginRequest.getUserMail(),
                    request
            );

            throw new InvalidCredentialsException(
                    "Invalid Credentials",
                    "BAD_CREDENTIALS",
                    HttpStatus.UNAUTHORIZED
            );
        }

        User user = optionalUser.get();
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
            auditLogService.log(
                    user,
                    Action.LOGIN,
                    "AUTH",
                    user.getUserId().toString(),
                    AuditStatus.FAILED,
                    "Invalid login credentials",
                    request

            );
            System.out.println("LOGIN FAILED");

            int attempts = updateFailedAttempts(user);

            if (attempts >= 3) {

                auditLogService.log(user, Action.LOGIN,"AUTH",user.getUserId().toString(),AuditStatus.LOCKED,"Account Locked due to multiple attempts to login",request);
                throw new RuntimeException(
                        "Account locked after 3 failed attempts. Please unlock via email."
                );
            } else {
                throw new InvalidCredentialsException(
                        "Invalid Credentials",
                        "BAD_CREDENTIALS",
                        HttpStatus.UNAUTHORIZED
                );
            }
        }

        User loggedInUser = optionalUser.get();


        String accessToken = jwtUtils.generateAccessToken(loggedInUser);
        sessionService.createSession(user, accessToken, request);

        RefreshToken refreshToken = createRefreshToken(loggedInUser,request);

        TokenAudit tokenAudit = new TokenAudit();
        tokenAudit.setAccessToken(HashUtil.sha256(accessToken));
        tokenAudit.setAction("Token generated for " + loggedInUser.getUserMail());
        tokenAudit.setTokenOwner(loggedInUser.getUserMail());
        tokenAuditRepository.save(tokenAudit);


        auditLogService.log(user, Action.LOGIN,"AUTH",user.getUserId().toString(),AuditStatus.SUCCESS,"LOGIN ATTEMPT SUCCESSFULLY ACCEPTED",request);
        String ip=auditLogService.extractIp(request);
        GeoLocation geo = geoIPService.getLocation(ip);
        String userAgentString =
                request.getHeader("User-Agent");



        UserAgent agent =
                userAgentAnalyzer.parse(userAgentString);

        String browser =
                agent.getValue("AgentName");
        String operatingSystem =
                agent.getValue("OperatingSystemName");


        String device;

        if (userAgentString.contains("Mobile")) {
            device = "Mobile";
        } else if (userAgentString.contains("Tablet")) {
            device = "Tablet";
        } else {
            device = "Desktop";
        }
        emailService.LoginMail(user.getUserMail(),user.getUserName(),ip,geo,device,browser,operatingSystem);


        return new JwtResponse(accessToken, refreshToken.getToken(), "Bearer");
    }


        public RefreshToken createRefreshToken(User user,HttpServletRequest httpServletRequest) {
            RefreshToken token = new RefreshToken();
            token.setUser(user);
            token.setToken(UUID.randomUUID().toString());
            token.setExpiryDate(Date.from(Instant.now().plusMillis(jwtUtils.getRefreshTokenExpiry())));

            RefreshToken savedToken =
                    refreshTokenRepository.save(token);

            long ttl =
                    savedToken.getExpiryDate().getTime()
                            - System.currentTimeMillis();

            refreshTokenCacheService.save(
                    savedToken.getToken(),
                    user.getUserId(),
                    ttl
            );


    
            auditLogService.log(
                    user,
                    Action.REFRESH_TOKEN_CREATED,
                    "AUTH",
                    user.getUserId().toString(),
                    AuditStatus.SUCCESS,
                    "Refresh token generated",
                    httpServletRequest
            );
    
            return savedToken;
        }





    public void logout(String refreshTokenStr, HttpServletRequest request) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Refresh Token Not Found",
                        "RESOURCE_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        User user = refreshToken.getUser();

        refreshTokenCacheService.deleteToken(refreshTokenStr);
        refreshTokenCacheService.removeTokenFromUser(user.getUserId(), refreshTokenStr);
        refreshTokenRepository.deleteByToken(refreshTokenStr);

        auditLogService.log(
                user,
                Action.LOGOUT,
                "AUTH",
                user.getUserId().toString(),
                AuditStatus.SUCCESS,
                "Logout success",
                request
        );

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            auditLogService.log(
                    user,
                    Action.SESSION_REVOKE,
                    "SESSION",
                    "MISSING_AUTH_HEADER",
                    AuditStatus.FAILED,
                    "Missing Authorization header during logout",
                    request
            );
            return;
        }

        String accessToken = authHeader.substring(7);

        try {

            Claims claims= jwtUtils.parseToken(accessToken);
            String jti = claims.getId();
            long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();

            if (ttl > 0) {

                Boolean exists = redisTemplate.hasKey("blacklist:jti:" + jti);
                if (Boolean.TRUE.equals(exists)) {
                    return;
                }
                redisTemplate.opsForValue().set(
                        "blacklist:jti:" + jti,
                        "true",
                        Duration.ofMillis(ttl)
                );
            }

            auditLogService.log(
                    user,
                    Action.SESSION_REVOKE,
                    "SESSION",
                    jti,
                    AuditStatus.SUCCESS,
                    "JWT revoked via blacklist",
                    request
            );

        } catch (ExpiredJwtException e) {

            auditLogService.log(
                    user,
                    Action.SESSION_REVOKE,
                    "SESSION",
                    "EXPIRED_JWT",
                    AuditStatus.FAILED,
                    "JWT already expired during logout",
                    request
            );

        } catch (JwtException e) {

            auditLogService.log(
                    user,
                    Action.SESSION_REVOKE,
                    "SESSION",
                    "INVALID_JWT",
                    AuditStatus.FAILED,
                    "JWT parsing failed during logout",
                    request
            );
        }
    }


    public void logoutAll(Authentication authentication, HttpServletRequest request) {

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        Integer userId = userDetails.getUserId();

        refreshTokenRepository.deleteByUser_UserId(userId);

        Set<Object> tokens = refreshTokenCacheService.getUserTokens(userId);

        if (tokens != null) {
            for (Object token : tokens) {
                refreshTokenCacheService.deleteToken(token.toString());
            }
        }

        refreshTokenCacheService.deleteUserTokenSet(userId);

        List<UserSession> sessions =
                userSessionRepository.findByUserUserIdAndRevokedFalse(userId);

        sessions.forEach(s -> s.setRevoked(true));
        userSessionRepository.saveAll(sessions);

        User user = userRepository.findById(userId).orElseThrow();

        auditLogService.log(
                user,
                Action.LOGOUT_ALL,
                "AUTH",
                userId.toString(),
                AuditStatus.SUCCESS,
                "Logout all devices",
                request
        );
    }







    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int updateFailedAttempts(User user) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);

        System.out.println(
                "Failed Attempts Before Save = "
                        + attempts
        );
        if (attempts >= 3) {
            user.setAccountNonLocked(false);
            user.setLockTime(java.time.LocalDateTime.now());
        }


        userRepository.save(user);
        return attempts;
    }

}