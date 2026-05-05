package com.rednorte.appointment_service.controller;

import com.rednorte.appointment_service.dto.AppointmentRequestDTO;
import com.rednorte.appointment_service.dto.AppointmentResponseDTO;
import com.rednorte.appointment_service.dto.CancelRequestDTO;
import com.rednorte.appointment_service.dto.MedicalRecordDTO;
import com.rednorte.appointment_service.enums.AppointmentPriority;
import com.rednorte.appointment_service.enums.AppointmentStatus;
import com.rednorte.appointment_service.mapper.AppointmentMapper;
import com.rednorte.appointment_service.model.Appointment;
import com.rednorte.appointment_service.service.AppointmentService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping
    public AppointmentResponseDTO create(@RequestBody AppointmentRequestDTO dto) {
        Appointment a = AppointmentMapper.toEntity(dto);
        Appointment saved = service.create(a);
        return AppointmentMapper.toDTO(saved);
    }

    @GetMapping
    public List<AppointmentResponseDTO> list() {
        return service.getAll().stream().map(AppointmentMapper::toDTO).toList();
    }

    @GetMapping("/patient/{rut}")
    public List<AppointmentResponseDTO> listByPatient(@PathVariable String rut) {
        return service.getByPatientRut(rut).stream().map(AppointmentMapper::toDTO).toList();
    }

    @GetMapping("/doctor/{rut}")
    public List<AppointmentResponseDTO> listByDoctor(@PathVariable String rut) {
        return service.getByDoctorRut(rut).stream().map(AppointmentMapper::toDTO).toList();
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id, @RequestBody CancelRequestDTO dto) {
        service.cancel(id, dto.getReason());
        return ResponseEntity.ok("Cita cancelada");
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        service.updateStatus(id, AppointmentStatus.valueOf(status.toUpperCase()));
        return ResponseEntity.ok("Estado actualizado");
    }

    @PutMapping("/{id}/priority")
    public ResponseEntity<?> updatePriority(@PathVariable Long id, @RequestParam String priority) {
        service.updatePriority(id, AppointmentPriority.valueOf(priority.toUpperCase()));
        return ResponseEntity.ok("Prioridad actualizada");
    }

    @PutMapping("/{id}/medical-record")
    public ResponseEntity<?> saveMedicalRecord(@PathVariable Long id, @RequestBody MedicalRecordDTO dto) {
        service.saveMedicalRecord(id, dto);
        return ResponseEntity.ok("Registro médico guardado");
    }
}