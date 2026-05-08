package com.rednorte.bff_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bff/doctor")
@RequiredArgsConstructor
public class DoctorBffController {

    private final WebClient webClient;

    @Value("${gateway.url}")
    private String gatewayUrl;

    private String getCurrentRut() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (String) auth.getPrincipal();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@RequestHeader("Authorization") String authHeader) {
        try {
            Object result = webClient.get()
                    .uri(gatewayUrl + "/api/users/rut/" + getCurrentRut())
                    .header("Authorization", authHeader)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/appointments")
    public ResponseEntity<?> getMyAppointments(@RequestHeader("Authorization") String authHeader) {
        try {
            List result = webClient.get()
                    .uri(gatewayUrl + "/api/appointments/doctor/" + getCurrentRut())
                    .header("Authorization", authHeader)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/appointments/{id}/priority")
    public ResponseEntity<?> updatePriority(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestParam String priority) {
        try {
            webClient.put()
                    .uri(gatewayUrl + "/api/appointments/" + id + "/priority?priority=" + priority)
                    .header("Authorization", authHeader)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return ResponseEntity.ok(Map.of("message", "Prioridad actualizada"));
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/appointments/{id}/medical-record")
    public ResponseEntity<?> saveMedicalRecord(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            webClient.put()
                    .uri(gatewayUrl + "/api/appointments/" + id + "/medical-record")
                    .header("Authorization", authHeader)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return ResponseEntity.ok(Map.of("message", "Registro médico guardado"));
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }
}