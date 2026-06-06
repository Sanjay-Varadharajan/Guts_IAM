package com.guts.Guts_IAM.security.config;

import com.guts.Guts_IAM.security.jwt.filter.JwtAuthenticationFilter;
import com.guts.Guts_IAM.security.userdetails.CustomUserDetailService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

    private final CustomUserDetailService customUserDetailService;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        return
                httpSecurity.csrf(csrf->csrf.disable())
                        .cors(Customizer.withDefaults())
                        .formLogin(form->form.disable())
                        .sessionManagement(sessionManagement->sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        )

                        .authorizeHttpRequests(authorize->
                                authorize
                                        .requestMatchers("/api/v1/manager/**").hasRole("MANAGER")
                                        .requestMatchers("/api/v1/employee/**").hasRole("EMPLOYEE")
                                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                                        .requestMatchers("/api/v1/guest/**").hasRole("GUEST")
                                        .requestMatchers("/api/v1/auditor/**").hasRole("AUDITOR")
                                .requestMatchers("/api/v1/auth/**").permitAll()
                                        .requestMatchers("/api/v1/unlock/**").permitAll()
                                        .anyRequest().authenticated())

                        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                        .exceptionHandling(exceptionHandling->exceptionHandling
                                .authenticationEntryPoint((request, response, authException) ->
                                {
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.setContentType("application/json");
                                    response.getWriter().write(
                                            """
                                            {
                                                "error": "TOKEN_EXPIRED_OR_INVALID",
                                                "message": "Please login again"
                                            }
                                            """
                                    );
                                })
                        ).build();
    }


    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider=new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(customUserDetailService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){

        return new BCryptPasswordEncoder();
    }

}
