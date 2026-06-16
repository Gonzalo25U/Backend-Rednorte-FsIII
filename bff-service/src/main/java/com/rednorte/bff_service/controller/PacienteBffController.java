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

import java.util.*;

@RestController
@RequestMapping("/bff/paciente")
@RequiredArgsConstructor
public class PacienteBffController {

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
            String rut = getCurrentRut();

            List<?> appointments = webClient.get()
                    .uri(gatewayUrl + "/api/appointments/patient/" + rut)
                    .header("Authorization", authHeader)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            List<?> doctors = webClient.get()
                    .uri(gatewayUrl + "/api/users/doctors")
                    .header("Authorization", authHeader)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            Map<String, String> rutToName = new HashMap<>();
            if (doctors != null) {
                for (Object d : doctors) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> doctor = (Map<String, Object>) d;
                    rutToName.put((String) doctor.get("rut"), (String) doctor.get("name"));
                }
            }

            if (appointments != null) {
                for (Object a : appointments) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> appt = (Map<String, Object>) a;
                    String doctorRut = (String) appt.get("doctorRut");
                    appt.put("doctorName", rutToName.getOrDefault(doctorRut, doctorRut));
                }
            }

            return ResponseEntity.ok(appointments);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/appointments")
    public ResponseEntity<?> createAppointment(@RequestHeader("Authorization") String authHeader) {
        try {
            String patientRut = getCurrentRut();

            List<?> doctors = webClient.get()
                    .uri(gatewayUrl + "/api/users/doctors")
                    .header("Authorization", authHeader)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            if (doctors == null || doctors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "No hay doctores disponibles"));
            }

            Map<String, Integer> pendingByDoctor = new HashMap<>();
            for (Object d : doctors) {
                @SuppressWarnings("unchecked")
                Map<String, Object> doctor = (Map<String, Object>) d;
                pendingByDoctor.put((String) doctor.get("rut"), 0);
            }

            List<?> allAppts = webClient.get()
                    .uri(gatewayUrl + "/api/appointments")
                    .header("Authorization", authHeader)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            if (allAppts != null) {
                for (Object a : allAppts) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> appt = (Map<String, Object>) a;
                    String doctorRut = (String) appt.get("doctorRut");
                    String status = (String) appt.get("status");
                    if ("PENDIENTE".equals(status) && pendingByDoctor.containsKey(doctorRut)) {
                        pendingByDoctor.put(doctorRut, pendingByDoctor.get(doctorRut) + 1);
                    }
                }
            }

            String assignedDoctorRut = pendingByDoctor.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse((String) ((Map<?, ?>) doctors.get(0)).get("rut"));

            String assignedDoctorName = assignedDoctorRut;
            for (Object d : doctors) {
                @SuppressWarnings("unchecked")
                Map<String, Object> doctor = (Map<String, Object>) d;
                if (assignedDoctorRut.equals(doctor.get("rut"))) {
                    assignedDoctorName = (String) doctor.get("name");
                    break;
                }
            }

            Map<String, Object> body = new HashMap<>();
            body.put("patientRut", patientRut);
            body.put("doctorRut", assignedDoctorRut);

            @SuppressWarnings("unchecked")
            Map<String, Object> result = webClient.post()
                    .uri(gatewayUrl + "/api/appointments")
                    .header("Authorization", authHeader)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            Map<String, Object> enriched = new HashMap<>(result);
            enriched.put("doctorName", assignedDoctorName);

            return ResponseEntity.ok(enriched);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getResponseBodyAsString()));
        }
    }

    @PutMapping("/appointments/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            webClient.put()
                    .uri(gatewayUrl + "/api/appointments/" + id + "/cancel")
                    .header("Authorization", authHeader)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return ResponseEntity.ok(Map.of("message", "Cita cancelada"));
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/appointments/{id}/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPatientImage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
            String fileName = id + "/paciente_" + UUID.randomUUID() + "_" + originalName;
            String uploadUrl = supabaseProperties.getUrl() + "/storage/v1/object/appointment-images/" + fileName;

            byte[] bytes = file.getBytes();
            webClient.post()
                    .uri(uploadUrl)
                    .header("Authorization", "Bearer " + supabaseProperties.getServiceKey())
                    .header("x-upsert", "true")
                    .header("Content-Type", file.getContentType())
                    .bodyValue(bytes)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            String publicUrl = supabaseProperties.getUrl() + "/storage/v1/object/public/appointment-images/" + fileName;

            webClient.put()
                    .uri(gatewayUrl + "/api/appointments/" + id + "/patient-image-url")
                    .header("Authorization", authHeader)
                    .bodyValue(Map.of("patientImageUrl", publicUrl))
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return ResponseEntity.ok(Map.of("patientImageUrl", publicUrl));

        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getMessage()));
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