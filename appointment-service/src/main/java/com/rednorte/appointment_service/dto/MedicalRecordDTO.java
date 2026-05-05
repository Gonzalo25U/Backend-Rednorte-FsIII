package com.rednorte.appointment_service.dto;

import lombok.Data;

@Data
public class MedicalRecordDTO {
    private String prescription;
    private String indications;
    private Integer restDays;
}