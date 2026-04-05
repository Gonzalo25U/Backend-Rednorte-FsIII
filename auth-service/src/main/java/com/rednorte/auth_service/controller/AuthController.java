package com.rednorte.auth_service.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.*;
import com.rednorte.auth_service.dto.LoginRequest;
import com.rednorte.auth_service.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {

        String token = service.login(request);

        return Map.of("token", token);
    }
}