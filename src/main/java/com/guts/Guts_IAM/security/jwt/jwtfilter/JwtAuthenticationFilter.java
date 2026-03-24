package com.guts.Guts_IAM.security.jwt.jwtfilter;

import com.guts.Guts_IAM.exceptionhandling.exceptions.UnauthorizedException;
import com.guts.Guts_IAM.security.jwt.jwtutils.JwtUtils;
import com.guts.Guts_IAM.security.userdetails.CustomUserDetailService;
import com.guts.Guts_IAM.security.userdetails.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
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
                if (jwtUtils.validateToken(token)) {
                    String userName = jwtUtils.getUsernameFromToken(token);
                    CustomUserDetails userDetails =
                            (CustomUserDetails) customUserDetailService.loadUserByUsername(userName);


                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, //principal
                                    null,  //we dont save credentials here
                                    userDetails.getAuthorities()
                            );
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }

            catch (io.jsonwebtoken.ExpiredJwtException e) {
                throw new UnauthorizedException("JWT token expired");
            } catch (io.jsonwebtoken.JwtException e) {
                throw new UnauthorizedException("JWT token invalid");
            } catch (Exception e) {
                throw new UnauthorizedException("Authentication failed: " + e.getMessage());
            }
        }
        filterChain.doFilter(request,response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.equals("/api/auth/login") || path.equals("/api/auth/refresh");
    }
}
