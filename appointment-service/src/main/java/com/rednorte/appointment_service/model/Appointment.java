package com.rednorte.appointment_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

import com.rednorte.appointment_service.enums.AppointmentPriority;
import com.rednorte.appointment_service.enums.AppointmentStatus;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String patientRut;

    @NotBlank
    private String doctorRut;

    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    @Enumerated(EnumType.STRING)
    private AppointmentPriority priority;

    private String cancelReason;

    // Campos médicos registrados por el doctor
    private String prescription;   // Receta médica
    private String indications;    // Indicaciones
    private Integer restDays;      // Días de reposo
}