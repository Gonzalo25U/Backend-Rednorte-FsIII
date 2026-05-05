package com.rednorte.bff_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/bff/doctor")
@RequiredArgsConstructor
public class DoctorBffController {

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

    private String getCurrentRut() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (String) auth.getPrincipal();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@RequestHeader("Authorization") String authHeader) {
        try {
            String rut = getCurrentRut();
            HttpEntity<?> request = new HttpEntity<>(buildHeaders(authHeader));
            ResponseEntity<Object> response = restTemplate.exchange(
                gatewayUrl + "/api/users/rut/" + rut,
                HttpMethod.GET,
                request,
                Object.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/appointments")
    public ResponseEntity<?> getMyAppointments(@RequestHeader("Authorization") String authHeader) {
        try {
            String rut = getCurrentRut();
            HttpEntity<?> request = new HttpEntity<>(buildHeaders(authHeader));
            ResponseEntity<Object[]> response = restTemplate.exchange(
                gatewayUrl + "/api/appointments/doctor/" + rut,
                HttpMethod.GET,
                request,
                Object[].class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/appointments/{id}/priority")
    public ResponseEntity<?> updatePriority(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestParam String priority) {
        try {
            HttpEntity<?> request = new HttpEntity<>(buildHeaders(authHeader));
            restTemplate.exchange(
                gatewayUrl + "/api/appointments/" + id + "/priority?priority=" + priority,
                HttpMethod.PUT,
                request,
                Void.class
            );
            return ResponseEntity.ok(Map.of("message", "Prioridad actualizada"));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/appointments/{id}/medical-record")
    public ResponseEntity<?> saveMedicalRecord(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, buildHeaders(authHeader));
            restTemplate.exchange(
                gatewayUrl + "/api/appointments/" + id + "/medical-record",
                HttpMethod.PUT,
                request,
                Void.class
            );
            return ResponseEntity.ok(Map.of("message", "Registro médico guardado"));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }
}