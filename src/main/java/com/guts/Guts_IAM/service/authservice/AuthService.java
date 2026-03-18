package com.guts.Guts_IAM.service.authservice;

import com.guts.Guts_IAM.enums.Roles;
import com.guts.Guts_IAM.exceptionhandling.exceptions.ResourceNotFoundException;
import com.guts.Guts_IAM.model.TokenAudit;
import com.guts.Guts_IAM.model.audits.AuditLog;
import com.guts.Guts_IAM.model.refreshtoken.RefreshToken;
import com.guts.Guts_IAM.model.user.User;
import com.guts.Guts_IAM.repo.auditrepo.AuditRepository;
import com.guts.Guts_IAM.repo.auditrepo.TokenAuditRepository;
import com.guts.Guts_IAM.repo.refreshtokenrepo.RefreshTokenRepository;
import com.guts.Guts_IAM.repo.userrepo.UserRepository;
import com.guts.Guts_IAM.security.jwt.jwtutils.JwtUtils;
import com.guts.Guts_IAM.security.signup.JwtResponse;
import com.guts.Guts_IAM.security.signup.LoginDto;
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

    public JwtResponse login(LoginDto loginDto) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUserMail(),
                        loginDto.getUserPassword()
                )
        );

        User user = userRepository.findByUserMailAndActiveTrue(loginDto.getUserMail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtUtils.generateAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user);
        TokenAudit tokenAudit=new TokenAudit();
        tokenAudit.setAccessToken(accessToken);
        tokenAudit.setAction("Token is generated for "+user.getUserMail());
        tokenAudit.setTokenOwner(user.getUserMail());
        tokenAuditRepository.save(tokenAudit);

        AuditLog auditLog=new AuditLog();
        auditLog.setLogAction(user.getUserMail()+" logged In");
        auditLog.setUserMail(user.getUserMail());
        auditLog.setUserRoles(user.getUserRoles());
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

    public JwtResponse refreshAccessToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.getExpiryDate().before(Date.from(Instant.now()))) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired. Please login again.");
        }

        String newAccessToken = jwtUtils.generateAccessToken(refreshToken.getUser());
        return new JwtResponse(newAccessToken, refreshToken.getToken(), "Bearer");
    }

    public void logout(String refreshTokenStr) {
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

        Set<Roles> rolesSet=refreshTokenCheck
                .get()
                .getUser()
                .getUserRoles();

        auditLog.setUserRoles(rolesSet);
        auditLog.setLogAction(userMail+" Logged Out");
        auditLog.setUserMail(userMail);

        auditRepo.save(auditLog);
        refreshTokenRepository.deleteByToken(refreshTokenStr);
    }
}