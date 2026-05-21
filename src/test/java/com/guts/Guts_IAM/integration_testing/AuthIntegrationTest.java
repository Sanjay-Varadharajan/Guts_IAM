package com.guts.Guts_IAM.integration_testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guts.Guts_IAM.auth.dto.LoginRequest;
import com.guts.Guts_IAM.common.mail.EmailService;
import com.guts.Guts_IAM.security.jwt.filter.JwtAuthenticationFilter;
import com.guts.Guts_IAM.security.jwt.util.JwtUtils;
import com.guts.Guts_IAM.token.refreshtoken.dto.TokenRefreshRequest;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    EmailService emailService;


    private User testUser;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUserMail("bro@gmail.com");
        testUser.setUserName("bro");
        testUser.setUserPassword(passwordEncoder.encode("1234")); // IMPORTANT
        testUser.setActive(true);
        testUser.setAccountNonLocked(true);
        testUser.setFailedAttempts(0);

        userRepository.save(testUser);
    }

    @Test
    void testLoginSuccess() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setUserMail("bro@gmail.com");
        request.setUserPassword("1234");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }
    @Test
    void testLoginWrongPassword() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setUserMail("bro@gmail.com");
        request.setUserPassword("wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()); // 401
    }

    @Test
    void testLoginUserNotFound() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setUserMail("nouser@gmail.com");
        request.setUserPassword("1234");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound()); // 404
    }

    @Test
    void testAccountLockAfterThreeAttempts() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setUserMail("bro@gmail.com");
        request.setUserPassword("wrong");

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }

        User updatedUser = userRepository.findByUserMailAndActiveTrue("bro@gmail.com").get();

        assert(!updatedUser.isAccountNonLocked()); // should be locked
    }

    @Test
    void testLogoutSuccess() throws Exception {

        // first login
        LoginRequest login = new LoginRequest();
        login.setUserMail("bro@gmail.com");
        login.setUserPassword("1234");

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = objectMapper.readTree(response)
                .get("refreshToken").asText();

        TokenRefreshRequest logoutRequest = new TokenRefreshRequest();
        logoutRequest.setRefreshToken(refreshToken);

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testLogoutInvalidToken() throws Exception {

        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("invalid-token");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound()); // 404
    }
}