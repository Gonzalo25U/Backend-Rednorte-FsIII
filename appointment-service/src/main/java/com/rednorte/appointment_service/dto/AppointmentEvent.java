package com.rednorte.appointment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentEvent {
    private String type;
    private Long appointmentId;
    private String patientRut;
    private String doctorRut;
}