package com.rednorte.auth_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET = "clave-super-secreta-clave-super-secreta";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(String rut, String role) {
        return Jwts.builder()
                .setSubject(rut)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}