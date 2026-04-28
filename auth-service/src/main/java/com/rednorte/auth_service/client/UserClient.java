package com.rednorte.auth_service.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.rednorte.auth_service.dto.UserResponse;
import org.springframework.http.ResponseEntity;
@Component
public class UserClient {

    private final RestTemplate restTemplate;

    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public UserResponse getUserByRut(String rut) {
        try {
            String url = "http://user-service:8081/users/rut/" + rut;
            System.out.println("🌐 Llamando a: " + url);
            
            ResponseEntity<UserResponse> response = restTemplate.getForEntity(url, UserResponse.class);
            
            System.out.println("📦 Status: " + response.getStatusCode());
            System.out.println("📦 Body: " + response.getBody());
            
            return response.getBody();
        } catch (Exception e) {
            System.out.println("💥 Error tipo: " + e.getClass().getName());
            System.out.println("💥 Error mensaje: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Usuario no encontrado");
        }
    }
}