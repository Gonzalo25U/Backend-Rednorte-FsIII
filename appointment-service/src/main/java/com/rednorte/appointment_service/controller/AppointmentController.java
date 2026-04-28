package com.rednorte.appointment_service.controller;

import com.rednorte.appointment_service.dto.AppointmentRequestDTO;
import com.rednorte.appointment_service.dto.AppointmentResponseDTO;
import com.rednorte.appointment_service.enums.AppointmentStatus;
import com.rednorte.appointment_service.mapper.AppointmentMapper;
import com.rednorte.appointment_service.model.Appointment;
import com.rednorte.appointment_service.service.AppointmentService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    // Crear cita - PACIENTE o ADMIN
    @PostMapping
    public AppointmentResponseDTO create(@RequestBody AppointmentRequestDTO dto) {
        Appointment a = AppointmentMapper.toEntity(dto);
        Appointment saved = service.create(a);
        return AppointmentMapper.toDTO(saved);
    }

    // Listar citas - todos los roles autenticados
    @GetMapping
    public List<AppointmentResponseDTO> list() {
        return service.getAll()
                .stream()
                .map(AppointmentMapper::toDTO)
                .toList();
    }

    // Cancelar cita con motivo - PACIENTE o ADMIN
    @PutMapping("/{id}/cancel")
    public void cancel(@PathVariable Long id, @RequestParam String reason) {
        service.cancel(id, reason);
    }

    // Cambiar estado - MEDICO o ADMIN (validado por SecurityConfig)
    @PutMapping("/{id}/status")
    public void updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        service.updateStatus(id, AppointmentStatus.valueOf(status.toUpperCase()));
    }
}