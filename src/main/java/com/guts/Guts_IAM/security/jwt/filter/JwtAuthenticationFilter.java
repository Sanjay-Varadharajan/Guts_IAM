package com.guts.Guts_IAM.security.jwt.filter;

import com.guts.Guts_IAM.security.jwt.util.JwtUtils;
import com.guts.Guts_IAM.security.userdetails.CustomUserDetailService;
import com.guts.Guts_IAM.security.userdetails.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    private final CustomUserDetailService customUserDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {


        String authHeader=request.getHeader("Authorization");
        String token=null;


        if(authHeader!=null && authHeader.startsWith("Bearer ")){
            token=authHeader.substring(7).trim();

            try {
                String userName = jwtUtils.getUsernameFromToken(token);

                CustomUserDetails userDetails =
                        (CustomUserDetails) customUserDetailService.loadUserByUsername(userName);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            } catch (Exception e) {
                log.error("JWT authentication failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request,response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        if (path == null) return false;

        return path.equals("/api/auth/");
    }
}
