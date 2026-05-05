package com.rednorte.bff_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/bff/admin")
@RequiredArgsConstructor
public class AdminBffController {

    private final RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    private HttpHeaders buildHeaders(String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authHeader != null) {
            headers.set("Authorization", authHeader);
        }
        return headers;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@RequestHeader("Authorization") String authHeader) {
        try {
            HttpEntity<?> request = new HttpEntity<>(buildHeaders(authHeader));
            ResponseEntity<Object[]> response = restTemplate.exchange(
                gatewayUrl + "/api/users",
                HttpMethod.GET,
                request,
                Object[].class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {
        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, buildHeaders(authHeader));
            ResponseEntity<Object> response = restTemplate.exchange(
                gatewayUrl + "/api/users",
                HttpMethod.POST,
                request,
                Object.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {
            HttpEntity<?> request = new HttpEntity<>(buildHeaders(authHeader));
            restTemplate.exchange(
                gatewayUrl + "/api/users/" + id,
                HttpMethod.DELETE,
                request,
                Void.class
            );
            return ResponseEntity.ok(Map.of("message", "Usuario eliminado"));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/users/password")
    public ResponseEntity<?> updatePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {
        try {
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, buildHeaders(authHeader));
            restTemplate.exchange(
                gatewayUrl + "/api/users/password",
                HttpMethod.PUT,
                request,
                Void.class
            );
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada"));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }
}