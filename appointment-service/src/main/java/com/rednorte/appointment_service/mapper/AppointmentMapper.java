package com.rednorte.appointment_service.mapper;

import com.rednorte.appointment_service.dto.AppointmentRequestDTO;
import com.rednorte.appointment_service.dto.AppointmentResponseDTO;
import com.rednorte.appointment_service.model.Appointment;
import com.rednorte.appointment_service.enums.AppointmentStatus;
import com.rednorte.appointment_service.enums.AppointmentPriority;

public class AppointmentMapper {

    public static Appointment toEntity(AppointmentRequestDTO dto) {
        Appointment a = new Appointment();

        a.setPatientRut(dto.getPatientRut());
        a.setDoctorRut(dto.getDoctorRut());

        // ✅ usar enums
        a.setStatus(AppointmentStatus.PENDIENTE);
        a.setPriority(AppointmentPriority.C); // default

        return a;
    }

    public static AppointmentResponseDTO toDTO(Appointment a) {
        return new AppointmentResponseDTO(
                a.getId(),
                a.getPatientRut(),
                a.getDoctorRut(),
                a.getDateTime(),
                a.getStatus().name(),     // ✅ enum → String
                a.getPriority().name()    // ✅ enum → String
        );
    }
}