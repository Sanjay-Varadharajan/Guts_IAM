package com.guts.Guts_IAM.service.authservice;

import com.guts.Guts_IAM.exceptionhandling.exceptions.ResourceNotFoundException;
import com.guts.Guts_IAM.model.TokenAudit;
import com.guts.Guts_IAM.model.audits.AuditLog;
import com.guts.Guts_IAM.model.refreshtoken.RefreshToken;
import com.guts.Guts_IAM.model.user.Role;
import com.guts.Guts_IAM.model.user.User;
import com.guts.Guts_IAM.repo.auditrepo.AuditRepository;
import com.guts.Guts_IAM.repo.auditrepo.TokenAuditRepository;
import com.guts.Guts_IAM.repo.refreshtokenrepo.RefreshTokenRepository;
import com.guts.Guts_IAM.repo.userrepo.UserRepository;
import com.guts.Guts_IAM.security.jwt.jwtutils.JwtUtils;
import com.guts.Guts_IAM.security.signup.JwtResponse;
import com.guts.Guts_IAM.security.signup.LoginDto;
import com.guts.Guts_IAM.security.utils.HashUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenAuditRepository tokenAuditRepository;
    private final AuditRepository auditRepo;

    public JwtResponse login(LoginDto loginDto, HttpServletRequest httpServletRequest) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUserMail(),
                        loginDto.getUserPassword()
                )
        );

        User user = userRepository.findByUserMailAndActiveTrue(loginDto.getUserMail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtUtils.generateAccessToken(user);
        String hashedToken= HashUtil.sha256(accessToken);
        RefreshToken refreshToken = createRefreshToken(user);
        TokenAudit tokenAudit=new TokenAudit();
        tokenAudit.setAccessToken(hashedToken);
        tokenAudit.setAction("Token is generated for "+user.getUserMail());
        tokenAudit.setTokenOwner(user.getUserMail());
        tokenAuditRepository.save(tokenAudit);

        AuditLog auditLog=new AuditLog();
        auditLog.setLogAction("LOGIN");
        auditLog.setUserMail(user.getUserMail());
        auditLog.setResource("AUTH");
        auditLog.setResourceId(user.getUserId().toString());
        auditLog.setRoleName(user.getRoles().toString());
        auditLog.setUserId(user.getUserId());
        auditLog.setIpAddress(httpServletRequest.getRemoteAddr());
        auditLog.setUserAgent(httpServletRequest.getHeader("User-Agent"));
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

    public JwtResponse refreshAccessToken(String refreshTokenStr, HttpServletRequest httpServletRequest) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.getExpiryDate().before(Date.from(Instant.now()))) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired. Please login again.");
        }
        String newAccessToken = jwtUtils.generateAccessToken(refreshToken.getUser());

        TokenAudit tokenAudit=new TokenAudit();
        tokenAudit.setTokenOwner(refreshToken.getUser().getUserMail());
        tokenAudit.setAction("REFRESH_TOKEN");
        String hashedNewToken=HashUtil.sha256(newAccessToken);

        AuditLog auditLog = new AuditLog();
        auditLog.setLogAction("REFRESH_TOKEN");
        auditLog.setUserMail(refreshToken.getUser().getUserMail());
        auditLog.setResource("AUTH");
        auditLog.setResourceId(refreshToken.getUser().getUserId().toString());
        auditLog.setUserId(refreshToken.getUser().getUserId());
        auditLog.setRoleName(refreshToken.getUser().getRoles().toString());
        auditLog.setIpAddress(httpServletRequest.getRemoteAddr());
        auditLog.setUserAgent(httpServletRequest.getHeader("User-Agent"));

        auditRepo.save(auditLog);
        return new JwtResponse(newAccessToken, refreshToken.getToken(), "Bearer");
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
}