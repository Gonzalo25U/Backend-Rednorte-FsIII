package com.rednorte.user_service.security;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final String SECRET = "clave-secreta-123456";

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String getRut(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getRole(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }
}