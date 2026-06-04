    package com.guts.Guts_IAM.auth.controller;


    import com.guts.Guts_IAM.auth.service.AuthService;
    import com.guts.Guts_IAM.auth.dto.LoginRequest;
    import com.guts.Guts_IAM.token.refreshtoken.dto.TokenRefreshRequest;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.Authentication;
    import org.springframework.transaction.annotation.Transactional;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;

    @RequestMapping("/api/v1/auth")
    @RestController
    @RequiredArgsConstructor
    @Transactional
    public class AuthController {

        private final AuthService authService;




        @PostMapping("/login")
        public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest httpServletRequest){
            return ResponseEntity.ok(authService.login(loginRequest,httpServletRequest));
        }

        @PostMapping("/logout")
        public ResponseEntity<?> logout(@Valid @RequestBody TokenRefreshRequest request, HttpServletRequest httpServletRequest) {
            authService.logout(request.getRefreshToken(),httpServletRequest);
            return ResponseEntity.ok("Logged out successfully");
        }

        @PostMapping("/logout-all")
        public ResponseEntity<?> logoutAll(
                Authentication authentication,
                HttpServletRequest request) {

            authService.logoutAll(authentication, request);

            return ResponseEntity.ok(
                    "Logged out from all devices"
            );
        }

    }
