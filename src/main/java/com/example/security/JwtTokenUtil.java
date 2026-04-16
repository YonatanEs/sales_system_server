package com.example.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class JwtTokenUtil {
   
    //private final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    @Value("${jwt.secret}")
    private String secret;
    
    private Key getSigningKey(){
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    public String generateToken(CustomUserDetails userDetails){
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("userId", userDetails.getId())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() 
                                + 1000 * 60 * 60 * 10))//por 10 horas
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /*public String extractUsername(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }*/
    
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    public Long extracUserId(String token){
        return extractAllClaims(token).get("userId", Long.class);
    }
    
     public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}
