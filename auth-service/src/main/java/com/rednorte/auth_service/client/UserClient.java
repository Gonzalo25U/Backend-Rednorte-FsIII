package com.rednorte.auth_service.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.rednorte.auth_service.dto.UserResponse;

@Component
public class UserClient {

    private final RestTemplate restTemplate;

    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public UserResponse getUserByRut(String rut) {
        try {
            return restTemplate.getForObject(
                "http://localhost:8081/users/" + rut,
                UserResponse.class
            );
        } catch (Exception e) {
            e.printStackTrace(); // 🔥 IMPORTANTE
            throw new RuntimeException("Usuario no encontrado");
        }
    }
}