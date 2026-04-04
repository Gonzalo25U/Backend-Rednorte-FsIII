package com.rednorte.appointment_service.controller;

import com.rednorte.appointment_service.dto.AppointmentRequestDTO;
import com.rednorte.appointment_service.dto.AppointmentResponseDTO;
import com.rednorte.appointment_service.enums.AppointmentStatus;
import com.rednorte.appointment_service.mapper.AppointmentMapper;
import com.rednorte.appointment_service.model.Appointment;
import com.rednorte.appointment_service.service.AppointmentService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    // ✅ Crear
    @PostMapping
    public AppointmentResponseDTO create(@RequestBody AppointmentRequestDTO dto) {
        Appointment a = AppointmentMapper.toEntity(dto);
        Appointment saved = service.create(a);
        return AppointmentMapper.toDTO(saved);
    }

    // ✅ Listar
    @GetMapping
    public List<AppointmentResponseDTO> list() {
        return service.getAll()
                .stream()
                .map(AppointmentMapper::toDTO)
                .toList();
    }

    // ✅ Cancelar cita (con motivo)
    @PutMapping("/{id}/cancel")
    public void cancel(@PathVariable Long id, @RequestParam String reason) {
        service.cancel(id, reason);
    }

    // ✅ Cambiar estado (APROBAR, etc.)
    @PutMapping("/{id}/status")
    public void updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestHeader("role") String role // 👈 temporal
    ) {
        if (!role.equals("DOCTOR")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo doctores pueden cambiar estado");
        }

        service.updateStatus(id, AppointmentStatus.valueOf(status.toUpperCase()));
    }
}