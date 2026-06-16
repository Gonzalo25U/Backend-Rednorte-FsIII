package com.rednorte.notification_service.controller;

import com.rednorte.notification_service.model.Notification;
import com.rednorte.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping("/{rut}")
    public List<Notification> getNotifications(@PathVariable String rut) {
        return service.getNotifications(rut);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        service.markAsRead(id);
        return ResponseEntity.ok("Notificación marcada como leída");
    }
}