package com.guts.Guts_IAM.security.jwt.jwtfilter;

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
                if(jwtUtils.validateToken(token)){
                    Integer userId=jwtUtils.getUserIdFromToken(token);

                    List<String> roles=jwtUtils.getRolesFromToken(token);

                    List<SimpleGrantedAuthority> authorities=roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());


                    UsernamePasswordAuthenticationToken authenticationToken=
                            new UsernamePasswordAuthenticationToken(
                                    userId, //principal
                                    null,  //we dont save credentials here
                                    authorities
                            );

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
            catch (Exception e){
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Invalid or Expired Token");
                return;
            }
        }
        filterChain.doFilter(request,response);
    }
}
