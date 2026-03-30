package com.rednorte.appointment_service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.rednorte.appointment_service.client.UserClient;
import com.rednorte.appointment_service.model.Appointment;
import com.rednorte.appointment_service.repository.AppointmentRepository;
import com.rednorte.appointment_service.enums.AppointmentStatus;

@Service
public class AppointmentService {

    private final AppointmentRepository repo;
    private final UserClient userClient;

    // Inyección de dependencias
    public AppointmentService(AppointmentRepository repo, UserClient userClient) {
        this.repo = repo;
        this.userClient = userClient;
    }

    //  Crear cita con validaciones completas
    public Appointment create(Appointment a) {

        if (a.getDateTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha no puede ser null");
        }

        // validar paciente
        if (!userClient.existsUser(a.getPatientRut())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no existe");
        }

        // validar doctor
        if (!userClient.existsUser(a.getDoctorRut())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor no existe");
        }

        if (a.getDateTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("No puedes agendar en el pasado");
        }

        a.setStatus(AppointmentStatus.PENDIENTE);

        return repo.save(a);
    }

    public List<Appointment> getAll() {
        return repo.findAll();
    }

    // ✅ Cancelar cita correctamente
    public void cancel(Long id, String reason) {
        Appointment a = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if (a.getStatus() == AppointmentStatus.CANCELADA) {
            throw new RuntimeException("La cita ya está cancelada");
        }

        a.setStatus(AppointmentStatus.CANCELADA);
        a.setCancelReason(reason);

        repo.save(a);
    }

    // ✅ Actualizar estado
    public void updateStatus(Long id, AppointmentStatus status) {
        Appointment a = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        a.setStatus(status);
        repo.save(a);
    }
}