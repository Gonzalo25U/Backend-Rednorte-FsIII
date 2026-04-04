package com.rednorte.appointment_service.dto;

import lombok.Data;


@Data
public class AppointmentRequestDTO {
    private String patientRut;
    private String doctorRut;


}