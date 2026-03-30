package com.rednorte.appointment_service.dto;

import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;


@Data
public class AppointmentRequestDTO {
    private String patientRut;
    private String doctorRut;
    // Formato ISO 8601 para fecha y hora
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateTime;
}