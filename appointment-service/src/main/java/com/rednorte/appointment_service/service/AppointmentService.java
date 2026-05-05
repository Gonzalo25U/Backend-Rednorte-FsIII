package com.rednorte.appointment_service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.rednorte.appointment_service.client.UserClient;
import com.rednorte.appointment_service.dto.MedicalRecordDTO;
import com.rednorte.appointment_service.model.Appointment;
import com.rednorte.appointment_service.repository.AppointmentRepository;
import com.rednorte.appointment_service.enums.AppointmentPriority;
import com.rednorte.appointment_service.enums.AppointmentStatus;

@Service
public class AppointmentService {

    private final AppointmentRepository repo;
    private final UserClient userClient;

    public AppointmentService(AppointmentRepository repo, UserClient userClient) {
        this.repo = repo;
        this.userClient = userClient;
    }

    public Appointment create(Appointment a) {
        if (a.getDateTime() == null) {
            a.setDateTime(LocalDateTime.now().plusHours(1));
        }

        if (!userClient.existsUser(a.getPatientRut())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no existe");
        }

        if (!userClient.existsUser(a.getDoctorRut())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor no existe");
        }

        if (a.getDateTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes agendar en el pasado");
        }

        a.setStatus(AppointmentStatus.PENDIENTE);
        return repo.save(a);
    }

    public List<Appointment> getAll() {
        return repo.findAll();
    }

    public List<Appointment> getByPatientRut(String patientRut) {
        return repo.findByPatientRut(patientRut);
    }

    public List<Appointment> getByDoctorRut(String doctorRut) {
        return repo.findByDoctorRut(doctorRut);
    }

    public void cancel(Long id, String reason) {
        Appointment a = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

        if (a.getStatus() == AppointmentStatus.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cita ya está cancelada");
        }

        a.setStatus(AppointmentStatus.CANCELADA);
        a.setCancelReason(reason);
        repo.save(a);
    }

    public void updateStatus(Long id, AppointmentStatus status) {
        Appointment a = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

        a.setStatus(status);
        repo.save(a);
    }

    public void updatePriority(Long id, AppointmentPriority priority) {
        Appointment a = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

        a.setPriority(priority);
        repo.save(a);
    }

    public void saveMedicalRecord(Long id, MedicalRecordDTO dto) {
        Appointment a = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

        a.setPrescription(dto.getPrescription());
        a.setIndications(dto.getIndications());
        a.setRestDays(dto.getRestDays());
        a.setStatus(AppointmentStatus.APROBADA);
        repo.save(a);
    }
}