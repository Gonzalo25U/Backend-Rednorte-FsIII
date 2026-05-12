package com.rednorte.user_service.service;

import com.rednorte.user_service.enums.UserRole;
import com.rednorte.user_service.exception.BadRequestException;
import com.rednorte.user_service.exception.NotFoundException;
import com.rednorte.user_service.model.User;
import com.rednorte.user_service.repository.UserRepository;
import com.rednorte.user_service.utils.RutValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService service;

    // ── Helper ────────────────────────────────────────────────────────────────

    private User buildUser(UserRole role) {
        User u = new User();
        u.setId(1L);
        u.setRut("11111111-1");   // cualquier rut — el validador se mockea
        u.setName("Test User");
        u.setRole(role);
        u.setActive(true);
        return u;
    }

    // ── create() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Crea un usuario correctamente")
        void shouldCreateUserSuccessfully() {
            User user = buildUser(UserRole.DOCTOR);

            try (MockedStatic<RutValidator> mock = mockStatic(RutValidator.class)) {
                mock.when(() -> RutValidator.isValid(anyString())).thenReturn(true);

                when(repository.findByRut(anyString())).thenReturn(Optional.empty());
                when(passwordEncoder.encode(anyString())).thenReturn("hashed");
                when(repository.save(any())).thenReturn(user);

                String[] result = service.create(user);

                assertThat(result[0]).isEqualTo("1");
                assertThat(result[1]).isNotBlank();
                verify(repository).save(user);
            }
        }

        @Test
        @DisplayName("Lanza excepción si el RUT ya está registrado")
        void shouldThrowWhenRutAlreadyExists() {
            User user = buildUser(UserRole.DOCTOR);

            try (MockedStatic<RutValidator> mock = mockStatic(RutValidator.class)) {
                mock.when(() -> RutValidator.isValid(anyString())).thenReturn(true);
                when(repository.findByRut(anyString())).thenReturn(Optional.of(user));

                assertThatThrownBy(() -> service.create(user))
                        .isInstanceOf(BadRequestException.class)
                        .hasMessageContaining("RUT ya está registrado");

                verify(repository, never()).save(any());
            }
        }

        @Test
        @DisplayName("Lanza excepción si ya existe un ADMIN")
        void shouldThrowWhenAdminAlreadyExists() {
            User user = buildUser(UserRole.ADMIN);

            try (MockedStatic<RutValidator> mock = mockStatic(RutValidator.class)) {
                mock.when(() -> RutValidator.isValid(anyString())).thenReturn(true);
                when(repository.findByRut(anyString())).thenReturn(Optional.empty());
                when(repository.existsByRole(UserRole.ADMIN)).thenReturn(true);

                assertThatThrownBy(() -> service.create(user))
                        .isInstanceOf(BadRequestException.class)
                        .hasMessageContaining("Ya existe un ADMIN");

                verify(repository, never()).save(any());
            }
        }

        @Test
        @DisplayName("Lanza excepción si el RUT es inválido")
        void shouldThrowWhenRutIsInvalid() {
            User user = buildUser(UserRole.DOCTOR);

            try (MockedStatic<RutValidator> mock = mockStatic(RutValidator.class)) {
                mock.when(() -> RutValidator.isValid(anyString())).thenReturn(false);

                assertThatThrownBy(() -> service.create(user))
                        .isInstanceOf(BadRequestException.class)
                        .hasMessageContaining("RUT inválido");

                verify(repository, never()).save(any());
            }
        }
    }

    // ── updatePassword() ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("updatePassword()")
    class UpdatePassword {

        @Test
        @DisplayName("Actualiza la contraseña correctamente")
        void shouldUpdatePasswordSuccessfully() {
            User user = buildUser(UserRole.DOCTOR);
            when(repository.findByRut(anyString())).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("nueva1234")).thenReturn("hashed_nueva");

            service.updatePassword("11111111-1", "nueva1234");

            assertThat(user.getPassword()).isEqualTo("hashed_nueva");
            verify(repository).save(user);
        }

        @Test
        @DisplayName("Lanza excepción si el usuario no existe")
        void shouldThrowWhenUserNotFound() {
            when(repository.findByRut(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updatePassword("11111111-1", "pass"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Usuario no encontrado");

            verify(repository, never()).save(any());
        }
    }

    // ── getAllUsers() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllUsers()")
    class GetAllUsers {

        @Test
        @DisplayName("Retorna la lista completa")
        void shouldReturnAllUsers() {
            when(repository.findAll()).thenReturn(List.of(buildUser(UserRole.DOCTOR), buildUser(UserRole.ADMIN)));

            assertThat(service.getAllUsers()).hasSize(2);
            verify(repository).findAll();
        }

        @Test
        @DisplayName("Retorna lista vacía si no hay usuarios")
        void shouldReturnEmptyList() {
            when(repository.findAll()).thenReturn(List.of());

            assertThat(service.getAllUsers()).isEmpty();
        }
    }

    // ── getByRut() ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getByRut()")
    class GetByRut {

        @Test
        @DisplayName("Retorna el usuario cuando existe")
        void shouldReturnUser() {
            User user = buildUser(UserRole.DOCTOR);
            when(repository.findByRut(anyString())).thenReturn(Optional.of(user));

            assertThat(service.getByRut("11111111-1").getRut()).isEqualTo("11111111-1");
        }

        @Test
        @DisplayName("Lanza excepción si no existe")
        void shouldThrowWhenNotFound() {
            when(repository.findByRut(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getByRut("11111111-1"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Usuario no encontrado");
        }
    }

    // ── deleteUser() ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteUser()")
    class DeleteUser {

        @Test
        @DisplayName("Elimina un usuario existente")
        void shouldDeleteUser() {
            when(repository.existsById(1L)).thenReturn(true);

            service.deleteUser(1L);

            verify(repository).deleteById(1L);
        }

        @Test
        @DisplayName("Lanza excepción si el id no existe")
        void shouldThrowWhenNotFound() {
            when(repository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> service.deleteUser(99L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Usuario no encontrado");

            verify(repository, never()).deleteById(any());
        }
    }
}