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
@RequestMapping("/bff/paciente")
@RequiredArgsConstructor
public class PacienteBffController {

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

    // RUT del paciente autenticado desde el JWT
    private String getCurrentRut() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (String) auth.getPrincipal();
    }

    // Ver información del paciente
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

    // Ver citas del paciente
    @GetMapping("/appointments")
    public ResponseEntity<?> getMyAppointments(@RequestHeader("Authorization") String authHeader) {
        try {
            String rut = getCurrentRut();
            HttpEntity<?> request = new HttpEntity<>(buildHeaders(authHeader));
            ResponseEntity<Object[]> response = restTemplate.exchange(
                gatewayUrl + "/api/appointments/patient/" + rut,
                HttpMethod.GET,
                request,
                Object[].class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }

    // Solicitar cita
    @PostMapping("/appointments")
    public ResponseEntity<?> createAppointment(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {
        try {
            // Forzar que el patientRut sea el del paciente autenticado
            body.put("patientRut", getCurrentRut());
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, buildHeaders(authHeader));
            ResponseEntity<Object> response = restTemplate.exchange(
                gatewayUrl + "/api/appointments",
                HttpMethod.POST,
                request,
                Object.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }

    // Cancelar cita con motivo
    @PutMapping("/appointments/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, buildHeaders(authHeader));
            restTemplate.exchange(
                gatewayUrl + "/api/appointments/" + id + "/cancel",
                HttpMethod.PUT,
                request,
                Void.class
            );
            return ResponseEntity.ok(Map.of("message", "Cita cancelada"));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }
}