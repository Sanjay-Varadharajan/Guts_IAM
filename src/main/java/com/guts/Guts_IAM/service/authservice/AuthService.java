package com.guts.Guts_IAM.service.authservice;

import com.guts.Guts_IAM.model.RefreshToken;
import com.guts.Guts_IAM.model.User;
import com.guts.Guts_IAM.repo.refreshtoken.RefreshTokenRepository;
import com.guts.Guts_IAM.repo.user.UserRepository;
import com.guts.Guts_IAM.security.jwt.jwtutils.JwtUtils;
import com.guts.Guts_IAM.security.signup.JwtResponse;
import com.guts.Guts_IAM.security.signup.LoginDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

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
        refreshTokenRepository.deleteByToken(refreshTokenStr);
    }
}