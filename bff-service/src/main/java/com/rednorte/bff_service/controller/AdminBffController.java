package com.rednorte.bff_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bff/admin")
@RequiredArgsConstructor
public class AdminBffController {

    private final WebClient webClient;

    @Value("${gateway.url}")
    private String gatewayUrl;

    private WebClient.RequestHeadersSpec<?> withAuth(WebClient.RequestHeadersSpec<?> spec, String authHeader) {
        return spec.header("Authorization", authHeader);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@RequestHeader("Authorization") String authHeader) {
        try {
            List result = webClient.get()
                    .uri(gatewayUrl + "/api/users")
                    .header("Authorization", authHeader)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {
        try {
            Object result = webClient.post()
                    .uri(gatewayUrl + "/api/users")
                    .header("Authorization", authHeader)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getResponseBodyAsString()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {
            webClient.delete()
                    .uri(gatewayUrl + "/api/users/" + id)
                    .header("Authorization", authHeader)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return ResponseEntity.ok(Map.of("message", "Usuario eliminado"));
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/users/password")
    public ResponseEntity<?> updatePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {
        try {
            webClient.put()
                    .uri(gatewayUrl + "/api/users/password")
                    .header("Authorization", authHeader)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada"));
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }
}