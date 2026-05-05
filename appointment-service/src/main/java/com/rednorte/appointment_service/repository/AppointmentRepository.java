package com.rednorte.appointment_service.repository;

import com.rednorte.appointment_service.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientRut(String patientRut);
    List<Appointment> findByDoctorRut(String doctorRut);
}