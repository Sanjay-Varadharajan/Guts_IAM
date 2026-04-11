package com.guts.Guts_IAM.token.service;

import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
import com.guts.Guts_IAM.security.util.hashutil.HashUtil;
import com.guts.Guts_IAM.security.jwt.dto.JwtResponse;
import com.guts.Guts_IAM.security.jwt.util.JwtUtils;
import com.guts.Guts_IAM.token.audit.TokenAudit;
import com.guts.Guts_IAM.token.audit.TokenAuditRepository;
import com.guts.Guts_IAM.token.refreshtoken.model.RefreshToken;
import com.guts.Guts_IAM.token.refreshtoken.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {


    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtils jwtUtils;
    private final AuditRepository auditRepository;
    private final TokenAuditRepository tokenAuditRepository;

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
        String hashedNewToken= HashUtil.sha256(newAccessToken);
        tokenAuditRepository.save(tokenAudit);

        AuditLog auditLog = new AuditLog();
        auditLog.setLogAction("REFRESH_TOKEN");
        auditLog.setUserMail(refreshToken.getUser().getUserMail());
        auditLog.setResource("AUTH");
        auditLog.setResourceId(refreshToken.getUser().getUserId().toString());
        auditLog.setUserId(refreshToken.getUser().getUserId());
        auditLog.setRoleName(refreshToken.getUser().getRoles().toString());
        auditLog.setIpAddress(httpServletRequest.getRemoteAddr());
        auditLog.setUserAgent(httpServletRequest.getHeader("User-Agent"));

        auditRepository.save(auditLog);
        return new JwtResponse(newAccessToken, refreshToken.getToken(), "Bearer");
    }
}
