package com.rednorte.auth_service.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

        UserResponse user = userClient.getUserByRut(request.getRut());

        if (user == null) {
            System.out.println("❌ Usuario no encontrado");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        if (!user.isActive()) {
            System.out.println("❌ Usuario inactivo");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario inactivo");
        }

        System.out.println("🔑 Password ingresada: " + request.getPassword());
        System.out.println("🔐 Password en BD: " + user.getPassword());

        boolean match = encoder.matches(request.getPassword(), user.getPassword());

        System.out.println("✅ Match: " + match);

        if (!match) {
            System.out.println("❌ Contraseña incorrecta");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(user.getRut(), user.getRole());
        System.out.println("🎟️ Token generado: " + token);

        return token;
    }
}