package com.guts.Guts_IAM.security.configuration;

import com.guts.Guts_IAM.security.jwt.jwtfilter.JwtAuthenticationFilter;
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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
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
                                authorize.requestMatchers("/api/auth/*").permitAll()
                                        .requestMatchers("/api/manager").hasRole("MANAGER")
                                        .requestMatchers("/api/employee").hasRole("EMPLOYEE")
                                        .requestMatchers("/api/admin").hasRole("ADMIN")
                                        .requestMatchers("/api/guest").hasRole("GUEST")
                                        .requestMatchers("/api/auditor").hasRole("AUDITOR")
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
                                                    "error" : "TOKEN_EXPIRED_OR_INVALID"
                                                    "message" : "Please login Again"
                                                    }
                                                    """
                                    );
                                })
                        ).build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider=new DaoAuthenticationProvider(customUserDetailService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration){
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
