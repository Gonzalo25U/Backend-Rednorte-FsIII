package com.rednorte.auth_service.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.rednorte.auth_service.client.UserClient;
import com.rednorte.auth_service.dto.LoginRequest;
import com.rednorte.auth_service.dto.UserResponse;
import com.rednorte.auth_service.security.JwtUtil;

@Service
public class AuthService {

    private final UserClient userClient;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserClient userClient, JwtUtil jwtUtil) {
        this.userClient = userClient;
        this.jwtUtil = jwtUtil;
    }

    public String login(LoginRequest request) {

        System.out.println("🔍 Buscando usuario con rut: " + request.getRut());

        UserResponse user = userClient.getUserByRut(request.getRut());

        System.out.println("👤 Usuario obtenido: " + user);

        if (user == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        System.out.println("🔑 Password ingresada: " + request.getPassword());
        System.out.println("🔐 Password en BD: " + user.getPassword());

        boolean match = encoder.matches(request.getPassword(), user.getPassword());

        System.out.println("✅ Password match: " + match);

        if (!match) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(user.getRut(), user.getRole());

        System.out.println("🎟️ Token generado: " + token);

        return token;
    }
}