    package com.guts.Guts_IAM.token.service;

    import com.guts.Guts_IAM.auditlog.action.AuditStatus;
    import com.guts.Guts_IAM.auditlog.model.AuditLog;
    import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
    import com.guts.Guts_IAM.auditlog.service.AuditLogService;
    import com.guts.Guts_IAM.common.exception.types.TokenNotFoundException;
    import com.guts.Guts_IAM.security.util.hashutil.HashUtil;
    import com.guts.Guts_IAM.security.jwt.dto.JwtResponse;
    import com.guts.Guts_IAM.security.jwt.util.JwtUtils;
    import com.guts.Guts_IAM.token.audit.TokenAudit;
    import com.guts.Guts_IAM.token.audit.TokenAuditRepository;
    import com.guts.Guts_IAM.token.refreshtoken.model.RefreshToken;
    import com.guts.Guts_IAM.token.refreshtoken.repository.RefreshTokenRepository;
    import com.guts.Guts_IAM.user.model.User;
    import com.guts.Guts_IAM.user.repository.UserRepository;
    import jakarta.servlet.http.HttpServletRequest;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.HttpStatus;
    import org.springframework.stereotype.Service;
    import org.springframework.web.server.ResponseStatusException;
    import com.guts.Guts_IAM.auditlog.action.Action;

    import java.time.Instant;
    import java.util.Date;
    import java.util.Optional;
    import java.util.Set;

    @Service
    @RequiredArgsConstructor
    public class RefreshTokenService {


        private final RefreshTokenRepository refreshTokenRepository;
        private final JwtUtils jwtUtils;
        private final TokenAuditRepository tokenAuditRepository;
        private final AuditLogService auditLogService;



        public JwtResponse refreshAccessToken(String refreshTokenStr, HttpServletRequest httpServletRequest) {
            Optional<RefreshToken> refreshToken1 = refreshTokenRepository.findByToken(refreshTokenStr);

            if(refreshToken1.isEmpty()){
                auditLogService.log(
                        null,
                        Action.REFRESH_ACCESS_TOKEN,
                        "AUTH",
                        null,
                        AuditStatus.FAILED,
                        "Refresh token not found",
                        httpServletRequest);

                throw new TokenNotFoundException("Refresh token not found", "NOT_FOUND",HttpStatus.NOT_FOUND);
            }

            RefreshToken refreshToken=refreshToken1.get();

            User user=refreshToken.getUser();

            if (refreshToken.getExpiryDate().before(Date.from(Instant.now()))) {
                refreshTokenRepository.delete(refreshToken);
                auditLogService.log(user,
                        Action.REFRESH_ACCESS_TOKEN,
                        "AUTH",
                        user.getUserId().toString(),
                        AuditStatus.EXPIRED,
                        "Refresh token expired and was revoked",
                        httpServletRequest);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
            }

            String newAccessToken = jwtUtils.generateAccessToken(refreshToken.getUser());

            TokenAudit tokenAudit=new TokenAudit();
            tokenAudit.setTokenOwner(refreshToken.getUser().getUserMail());
            tokenAudit.setAction("REFRESH_ACCESS_TOKEN");
            String hashedNewToken= HashUtil.sha256(newAccessToken);
            tokenAudit.setAccessToken(hashedNewToken);
            tokenAuditRepository.save(tokenAudit);


            auditLogService.log(user,
                    Action.REFRESH_ACCESS_TOKEN,
                    "AUTH",
                    user.getUserId().toString(),
                    AuditStatus.SUCCESS,
                    "Access token refreshed successfully",
                    httpServletRequest);


            return new JwtResponse(newAccessToken, refreshToken.getToken(), "Bearer");
        }
    }
