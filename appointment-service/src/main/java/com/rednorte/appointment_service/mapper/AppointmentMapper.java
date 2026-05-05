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
        a.setStatus(AppointmentStatus.PENDIENTE);
        a.setPriority(AppointmentPriority.C);
        return a;
    }

    public static AppointmentResponseDTO toDTO(Appointment a) {
        return new AppointmentResponseDTO(
                a.getId(),
                a.getPatientRut(),
                a.getDoctorRut(),
                a.getDateTime(),
                a.getStatus().name(),
                a.getPriority().name(),
                a.getCancelReason(),
                a.getPrescription(),
                a.getIndications(),
                a.getRestDays()
        );
    }
}