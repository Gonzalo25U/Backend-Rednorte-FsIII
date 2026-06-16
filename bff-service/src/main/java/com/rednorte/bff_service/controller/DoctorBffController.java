package com.rednorte.bff_service.controller;

import com.rednorte.bff_service.config.SupabaseProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/bff/doctor")
@RequiredArgsConstructor
public class DoctorBffController {

    private final WebClient webClient;
    private final SupabaseProperties supabaseProperties;

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

    @PostMapping(value = "/appointments/{id}/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
            String fileName = id + "/" + UUID.randomUUID() + "_" + originalName;
            String uploadUrl = supabaseProperties.getUrl() + "/storage/v1/object/appointment-images/" + fileName;

            byte[] bytes = file.getBytes();
            try {
                webClient.post()
                        .uri(uploadUrl)
                        .header("Authorization", "Bearer " + supabaseProperties.getServiceKey())
                        .header("Content-Type", file.getContentType())
                        .header("x-upsert", "true")
                        .bodyValue(bytes)
                        .retrieve()
                        .toBodilessEntity()
                        .block();
            } catch (WebClientResponseException ex) {
                System.err.println("=== SUPABASE ERROR ===");
                System.err.println("Status: " + ex.getStatusCode());
                System.err.println("Body: " + ex.getResponseBodyAsString());
                throw ex;
}

            String publicUrl = supabaseProperties.getUrl() + "/storage/v1/object/public/appointment-images/" + fileName;

            webClient.put()
                    .uri(gatewayUrl + "/api/appointments/" + id + "/image-url")
                    .header("Authorization", authHeader)
                    .bodyValue(Map.of("imageUrl", publicUrl))
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return ResponseEntity.ok(Map.of("imageUrl", publicUrl));

        } catch (WebClientResponseException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("error", e.getResponseBodyAsString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al subir imagen: " + e.getMessage()));
        }

        
    }
    @GetMapping("/notifications")
public ResponseEntity<?> getNotifications(@RequestHeader("Authorization") String authHeader) {
    try {
        List result = webClient.get()
                .uri(gatewayUrl + "/api/notifications/" + getCurrentRut())
                .header("Authorization", authHeader)
                .retrieve()
                .bodyToMono(List.class)
                .block();
        return ResponseEntity.ok(result);
    } catch (WebClientResponseException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
    }
}

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<?> markNotificationAsRead(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {
            webClient.put()
                    .uri(gatewayUrl + "/api/notifications/" + id + "/read")
                    .header("Authorization", authHeader)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return ResponseEntity.ok(Map.of("message", "Notificación marcada como leída"));
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }
}