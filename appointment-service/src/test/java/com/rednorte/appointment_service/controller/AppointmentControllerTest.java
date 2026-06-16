package com.rednorte.appointment_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rednorte.appointment_service.dto.AppointmentResponseDTO;
import com.rednorte.appointment_service.dto.CancelRequestDTO;
import com.rednorte.appointment_service.enums.AppointmentPriority;
import com.rednorte.appointment_service.enums.AppointmentStatus;
import com.rednorte.appointment_service.mapper.AppointmentMapper;
import com.rednorte.appointment_service.model.Appointment;
import com.rednorte.appointment_service.service.AppointmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService service;

    @Autowired
    private ObjectMapper objectMapper;

    // ── Helper ────────────────────────────────────────────────────────────────

    private Appointment buildAppointment() {
        Appointment a = new Appointment();
        a.setId(1L);
        a.setPatientRut("11111111-1");
        a.setDoctorRut("22222222-2");
        a.setDateTime(LocalDateTime.now().plusDays(1));
        a.setStatus(AppointmentStatus.PENDIENTE);
        return a;
    }

    private AppointmentResponseDTO buildResponseDTO() {
        return new AppointmentResponseDTO(
                1L, "11111111-1", "22222222-2",
                LocalDateTime.now().plusDays(1),
                "PENDIENTE", null, null, null, null, null, null, null
        );
    }

    // ── POST /appointments ────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /appointments")
    class Create {

        @Test
        @WithMockUser
        @DisplayName("Crea una cita y retorna 200")
        void shouldCreateAppointment() throws Exception {
            String body = """
                    { "patientRut": "11111111-1", "doctorRut": "22222222-2" }
                    """;
            Appointment saved = buildAppointment();
            AppointmentResponseDTO dto = buildResponseDTO();

            when(service.create(any())).thenReturn(saved);

            try (MockedStatic<AppointmentMapper> m = mockStatic(AppointmentMapper.class)) {
                m.when(() -> AppointmentMapper.toEntity(any())).thenReturn(saved);
                m.when(() -> AppointmentMapper.toDTO(saved)).thenReturn(dto);

                mockMvc.perform(post("/appointments")
                               .with(csrf())
                               .contentType(MediaType.APPLICATION_JSON)
                               .content(body))
                       .andExpect(status().isOk())
                       .andExpect(jsonPath("$.patientRut").value("11111111-1"));
            }
        }
    }

    // ── GET /appointments ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /appointments")
    class List_ {

        @Test
        @WithMockUser
        @DisplayName("Retorna todas las citas")
        void shouldReturnAll() throws Exception {
            when(service.getAll()).thenReturn(List.of(buildAppointment()));

            try (MockedStatic<AppointmentMapper> m = mockStatic(AppointmentMapper.class)) {
                m.when(() -> AppointmentMapper.toDTO(any())).thenReturn(buildResponseDTO());

                mockMvc.perform(get("/appointments"))
                       .andExpect(status().isOk())
                       .andExpect(jsonPath("$.length()").value(1));
            }
        }
    }

    // ── GET /appointments/patient/{rut} ───────────────────────────────────────

    @Nested
    @DisplayName("GET /appointments/patient/{rut}")
    class ListByPatient {

        @Test
        @WithMockUser
        @DisplayName("Retorna citas del paciente")
        void shouldReturnByPatient() throws Exception {
            when(service.getByPatientRut("11111111-1")).thenReturn(List.of(buildAppointment()));

            try (MockedStatic<AppointmentMapper> m = mockStatic(AppointmentMapper.class)) {
                m.when(() -> AppointmentMapper.toDTO(any())).thenReturn(buildResponseDTO());

                mockMvc.perform(get("/appointments/patient/11111111-1"))
                       .andExpect(status().isOk())
                       .andExpect(jsonPath("$.length()").value(1));
            }
        }
    }

    // ── PUT /appointments/{id}/cancel ─────────────────────────────────────────

    @Nested
    @DisplayName("PUT /appointments/{id}/cancel")
    class Cancel {

        @Test
        @WithMockUser
        @DisplayName("Cancela una cita y retorna 200")
        void shouldCancelAppointment() throws Exception {
            CancelRequestDTO dto = new CancelRequestDTO();
            dto.setReason("No puede asistir");

            doNothing().when(service).cancel(1L, "No puede asistir");

            mockMvc.perform(put("/appointments/1/cancel")
                           .with(csrf())
                           .contentType(MediaType.APPLICATION_JSON)
                           .content(objectMapper.writeValueAsString(dto)))
                   .andExpect(status().isOk())
                   .andExpect(content().string("Cita cancelada"));
        }

        @Test
        @WithMockUser
        @DisplayName("Retorna 404 si la cita no existe")
        void shouldReturn404WhenNotFound() throws Exception {
            CancelRequestDTO dto = new CancelRequestDTO();
            dto.setReason("motivo");

            doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"))
                    .when(service).cancel(eq(99L), anyString());

            mockMvc.perform(put("/appointments/99/cancel")
                           .with(csrf())
                           .contentType(MediaType.APPLICATION_JSON)
                           .content(objectMapper.writeValueAsString(dto)))
                   .andExpect(status().isNotFound());
        }
    }

    // ── PUT /appointments/{id}/status ─────────────────────────────────────────

    @Nested
    @DisplayName("PUT /appointments/{id}/status")
    class UpdateStatus {

        @Test
        @WithMockUser
        @DisplayName("Actualiza el estado y retorna 200")
        void shouldUpdateStatus() throws Exception {
            doNothing().when(service).updateStatus(1L, AppointmentStatus.APROBADA);

            mockMvc.perform(put("/appointments/1/status")
                           .with(csrf())
                           .param("status", "APROBADA"))
                   .andExpect(status().isOk())
                   .andExpect(content().string("Estado actualizado"));
        }
    }

    // ── PUT /appointments/{id}/priority ──────────────────────────────────────

    @Nested
    @DisplayName("PUT /appointments/{id}/priority")
    class UpdatePriority {

        @Test
        @WithMockUser
        @DisplayName("Actualiza la prioridad y retorna 200")
        void shouldUpdatePriority() throws Exception {
            doNothing().when(service).updatePriority(1L, AppointmentPriority.A);

            mockMvc.perform(put("/appointments/1/priority")
                           .with(csrf())
                           .param("priority", "A"))
                   .andExpect(status().isOk())
                   .andExpect(content().string("Prioridad actualizada"));
        }
    }
    // ── GET /appointments/doctor/{rut} ────────────────────────────────────────

@Nested
@DisplayName("GET /appointments/doctor/{rut}")
class ListByDoctor {

    @Test
    @WithMockUser
    @DisplayName("Retorna citas del doctor")
    void shouldReturnByDoctor() throws Exception {
        when(service.getByDoctorRut("22222222-2")).thenReturn(List.of(buildAppointment()));

        try (MockedStatic<AppointmentMapper> m = mockStatic(AppointmentMapper.class)) {
            m.when(() -> AppointmentMapper.toDTO(any())).thenReturn(buildResponseDTO());

            mockMvc.perform(get("/appointments/doctor/22222222-2"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.length()").value(1));
        }
    }
}

// ── PUT /appointments/{id}/medical-record ────────────────────────────────

@Nested
@DisplayName("PUT /appointments/{id}/medical-record")
class SaveMedicalRecord {

    @Test
    @WithMockUser
    @DisplayName("Guarda el registro médico y retorna 200")
    void shouldSaveMedicalRecord() throws Exception {
        String body = """
                { "prescription": "Ibuprofeno", "indications": "Cada 8h", "restDays": 3 }
                """;
        doNothing().when(service).saveMedicalRecord(eq(1L), any());

        mockMvc.perform(put("/appointments/1/medical-record")
                       .with(csrf())
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(body))
               .andExpect(status().isOk())
               .andExpect(content().string("Registro médico guardado"));
    }
}

// ── PUT /appointments/{id}/image-url ─────────────────────────────────────

@Nested
@DisplayName("PUT /appointments/{id}/image-url")
class SaveImageUrl {

    @Test
    @WithMockUser
    @DisplayName("Guarda la URL de imagen del médico y retorna 200")
    void shouldSaveImageUrl() throws Exception {
        String body = """
                { "imageUrl": "https://supabase.co/imagen.jpg" }
                """;
        doNothing().when(service).saveImageUrl(eq(1L), anyString());

        mockMvc.perform(put("/appointments/1/image-url")
                       .with(csrf())
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(body))
               .andExpect(status().isOk())
               .andExpect(content().string("Imagen del médico guardada"));
    }
}

    // ── PUT /appointments/{id}/patient-image-url ─────────────────────────────

    @Nested
    @DisplayName("PUT /appointments/{id}/patient-image-url")
    class SavePatientImageUrl {

        @Test
        @WithMockUser
        @DisplayName("Guarda la URL de imagen del paciente y retorna 200")
        void shouldSavePatientImageUrl() throws Exception {
            String body = """
                    { "patientImageUrl": "https://supabase.co/paciente.jpg" }
                    """;
            doNothing().when(service).savePatientImageUrl(eq(1L), anyString());

            mockMvc.perform(put("/appointments/1/patient-image-url")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("Imagen del paciente guardada"));
        }
    }
}