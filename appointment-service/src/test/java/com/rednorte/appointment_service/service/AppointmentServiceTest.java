package com.rednorte.appointment_service.service;

import com.rednorte.appointment_service.client.UserClient;
import com.rednorte.appointment_service.dto.MedicalRecordDTO;
import com.rednorte.appointment_service.enums.AppointmentPriority;
import com.rednorte.appointment_service.enums.AppointmentStatus;
import com.rednorte.appointment_service.model.Appointment;
import com.rednorte.appointment_service.repository.AppointmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository repo;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private AppointmentService service;

    // ── Helper ────────────────────────────────────────────────────────────────

    private Appointment buildAppointment() {
        Appointment a = new Appointment();
        a.setId(1L);
        a.setPatientRut("11111111-1");
        a.setDoctorRut("22222222-2");
        a.setDateTime(LocalDateTime.now().plusDays(1)); // siempre en el futuro
        a.setStatus(AppointmentStatus.PENDIENTE);
        return a;
    }

    // ── create() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Crea una cita correctamente")
        void shouldCreateAppointmentSuccessfully() {
            Appointment a = buildAppointment();

            when(userClient.existsUser(a.getPatientRut())).thenReturn(true);
            when(userClient.existsUser(a.getDoctorRut())).thenReturn(true);
            when(repo.save(any())).thenReturn(a);

            Appointment result = service.create(a);

            assertThat(result.getStatus()).isEqualTo(AppointmentStatus.PENDIENTE);
            verify(repo).save(a);
        }

        @Test
        @DisplayName("Asigna fecha automática si no se provee")
        void shouldSetDefaultDateTimeWhenNull() {
            Appointment a = buildAppointment();
            a.setDateTime(null);

            when(userClient.existsUser(anyString())).thenReturn(true);
            when(repo.save(any())).thenReturn(a);

            service.create(a);

            assertThat(a.getDateTime()).isNotNull();
            verify(repo).save(a);
        }

        @Test
        @DisplayName("Lanza excepción si el paciente no existe")
        void shouldThrowWhenPatientNotFound() {
            Appointment a = buildAppointment();

            when(userClient.existsUser(a.getPatientRut())).thenReturn(false);

            assertThatThrownBy(() -> service.create(a))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Paciente no existe");

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("Lanza excepción si el doctor no existe")
        void shouldThrowWhenDoctorNotFound() {
            Appointment a = buildAppointment();

            when(userClient.existsUser(a.getPatientRut())).thenReturn(true);
            when(userClient.existsUser(a.getDoctorRut())).thenReturn(false);

            assertThatThrownBy(() -> service.create(a))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Doctor no existe");

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("Lanza excepción si la fecha es en el pasado")
        void shouldThrowWhenDateIsInPast() {
            Appointment a = buildAppointment();
            a.setDateTime(LocalDateTime.now().minusDays(1));

            when(userClient.existsUser(anyString())).thenReturn(true);

            assertThatThrownBy(() -> service.create(a))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("No puedes agendar en el pasado");

            verify(repo, never()).save(any());
        }
    }

    // ── getAll() ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("Retorna todas las citas")
        void shouldReturnAll() {
            when(repo.findAll()).thenReturn(List.of(buildAppointment(), buildAppointment()));

            assertThat(service.getAll()).hasSize(2);
            verify(repo).findAll();
        }

        @Test
        @DisplayName("Retorna lista vacía si no hay citas")
        void shouldReturnEmpty() {
            when(repo.findAll()).thenReturn(List.of());

            assertThat(service.getAll()).isEmpty();
        }
    }

    // ── getByPatientRut() / getByDoctorRut() ──────────────────────────────────

    @Nested
    @DisplayName("getByPatientRut() / getByDoctorRut()")
    class GetByRut {

        @Test
        @DisplayName("Retorna citas del paciente")
        void shouldReturnByPatient() {
            when(repo.findByPatientRut("11111111-1")).thenReturn(List.of(buildAppointment()));

            assertThat(service.getByPatientRut("11111111-1")).hasSize(1);
        }

        @Test
        @DisplayName("Retorna citas del doctor")
        void shouldReturnByDoctor() {
            when(repo.findByDoctorRut("22222222-2")).thenReturn(List.of(buildAppointment()));

            assertThat(service.getByDoctorRut("22222222-2")).hasSize(1);
        }
    }

    // ── cancel() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("cancel()")
    class Cancel {

        @Test
        @DisplayName("Cancela una cita correctamente")
        void shouldCancelAppointment() {
            Appointment a = buildAppointment();
            when(repo.findById(1L)).thenReturn(Optional.of(a));

            service.cancel(1L, "Motivo de prueba");

            assertThat(a.getStatus()).isEqualTo(AppointmentStatus.CANCELADA);
            assertThat(a.getCancelReason()).isEqualTo("Motivo de prueba");
            verify(repo).save(a);
        }

        @Test
        @DisplayName("Lanza excepción si la cita no existe")
        void shouldThrowWhenNotFound() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cancel(99L, "motivo"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Cita no encontrada");
        }

        @Test
        @DisplayName("Lanza excepción si la cita ya está cancelada")
        void shouldThrowWhenAlreadyCancelled() {
            Appointment a = buildAppointment();
            a.setStatus(AppointmentStatus.CANCELADA);
            when(repo.findById(1L)).thenReturn(Optional.of(a));

            assertThatThrownBy(() -> service.cancel(1L, "motivo"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("ya está cancelada");

            verify(repo, never()).save(any());
        }
    }

    // ── updateStatus() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatus {

        @Test
        @DisplayName("Actualiza el estado correctamente")
        void shouldUpdateStatus() {
            Appointment a = buildAppointment();
            when(repo.findById(1L)).thenReturn(Optional.of(a));

            service.updateStatus(1L, AppointmentStatus.APROBADA);

            assertThat(a.getStatus()).isEqualTo(AppointmentStatus.APROBADA);
            verify(repo).save(a);
        }

        @Test
        @DisplayName("Lanza excepción si la cita no existe")
        void shouldThrowWhenNotFound() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateStatus(99L, AppointmentStatus.APROBADA))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Cita no encontrada");
        }
    }

    // ── updatePriority() ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("updatePriority()")
    class UpdatePriority {

        @Test
        @DisplayName("Actualiza la prioridad correctamente")
        void shouldUpdatePriority() {
            Appointment a = buildAppointment();
            when(repo.findById(1L)).thenReturn(Optional.of(a));

            service.updatePriority(1L, AppointmentPriority.A);

            assertThat(a.getPriority()).isEqualTo(AppointmentPriority.A);
            verify(repo).save(a);
        }

        @Test
        @DisplayName("Lanza excepción si la cita no existe")
        void shouldThrowWhenNotFound() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updatePriority(99L, AppointmentPriority.A))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Cita no encontrada");
        }
    }

    // ── saveMedicalRecord() ───────────────────────────────────────────────────

    @Nested
    @DisplayName("saveMedicalRecord()")
    class SaveMedicalRecord {

        @Test
        @DisplayName("Guarda el registro médico correctamente")
        void shouldSaveMedicalRecord() {
            Appointment a = buildAppointment();
            when(repo.findById(1L)).thenReturn(Optional.of(a));

            MedicalRecordDTO dto = new MedicalRecordDTO();
            dto.setPrescription("Ibuprofeno 400mg");
            dto.setIndications("Tomar cada 8 horas");
            dto.setRestDays(3);

            service.saveMedicalRecord(1L, dto);

            assertThat(a.getPrescription()).isEqualTo("Ibuprofeno 400mg");
            assertThat(a.getIndications()).isEqualTo("Tomar cada 8 horas");
            assertThat(a.getRestDays()).isEqualTo(3);
            assertThat(a.getStatus()).isEqualTo(AppointmentStatus.APROBADA);
            verify(repo).save(a);
        }

        @Test
        @DisplayName("Lanza excepción si la cita no existe")
        void shouldThrowWhenNotFound() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.saveMedicalRecord(99L, new MedicalRecordDTO()))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Cita no encontrada");
        }
    }
}