package com.rednorte.appointment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AppointmentResponseDTO {

    private Long id;
    private String patientRut;
    private String doctorRut;
    private LocalDateTime dateTime;
    private String status;
    private String priority;
}