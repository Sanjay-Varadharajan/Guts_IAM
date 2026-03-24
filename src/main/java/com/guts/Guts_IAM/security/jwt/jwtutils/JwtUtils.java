package com.guts.Guts_IAM.security.jwt.jwtutils;


import com.guts.Guts_IAM.model.user.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {

    private final Key key;
    private final long ACCESS_TOKEN_EXPIRY=15*60*1000; //15 minutes fucker
    private final long REFRESH_TOKEN_EXPIRY=7*24*60*60*1000L; //7 days

    public JwtUtils(@Value("${jwt.secret}")String secret){
        this.key= Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUserMail())
                .claim("roles", user.getRoles().stream().map(role->role.getName()).toList())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+ACCESS_TOKEN_EXPIRY))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

        public String generateRefreshToken(User user){
            return Jwts.builder()
                    .setSubject(user.getUserMail())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis()+REFRESH_TOKEN_EXPIRY))
                    .signWith(key,SignatureAlgorithm.HS256)
                    .compact();
    }

    public Integer getUserIdFromToken(String token){
        return Integer.parseInt(Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject());
    }
    public String getUsernameFromToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public List<String> getRolesFromToken(String token){
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .get("roles",List.class);
    }


    public boolean validateToken(String token){
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public long getRefreshTokenExpiry() {
        return REFRESH_TOKEN_EXPIRY;
    }

}
