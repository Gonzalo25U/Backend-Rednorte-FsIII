package com.rednorte.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rednorte.user_service.dto.PasswordUpdateDTO;
import com.rednorte.user_service.dto.UserResponseDTO;
import com.rednorte.user_service.enums.UserRole;
import com.rednorte.user_service.exception.BadRequestException;
import com.rednorte.user_service.exception.NotFoundException;
import com.rednorte.user_service.mapper.UserMapper;
import com.rednorte.user_service.model.User;
import com.rednorte.user_service.service.UserService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * @WebMvcTest levanta solo la capa web (sin BD ni contexto completo).
 * Spring Security está activo; usamos @WithMockUser para simular autenticación
 * y csrf() en las peticiones que modifican estado (POST, PUT, DELETE).
 *
 * Si tu SecurityConfig excluye ciertos paths de autenticación, puedes
 * reemplazar @WithMockUser por una config de test propia con @TestConfiguration.
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService service;

    @Autowired
    private ObjectMapper objectMapper;

    // ── Helpers ──────────────────────────────────────────────────────────────

    private User buildUser(Long id, String rut, String name, UserRole role) {
        User u = new User();
        u.setId(id);
        u.setRut(rut);
        u.setName(name);
        u.setRole(role);
        u.setActive(true);
        return u;
    }

    private UserResponseDTO buildResponseDTO(Long id, String rut, String name,
                                             UserRole role, String generatedPassword) {
        return new UserResponseDTO(id, rut, name, role, true, generatedPassword);
    }

    // ── POST /users ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /users")
    class CreateUser {

        @Test
        @WithMockUser
        @DisplayName("Crea usuario y retorna 200 con la contraseña generada")
        void shouldCreateUserAndReturnDTO() throws Exception {
            String body = """
                    {
                        "rut": "12345678-9",
                        "name": "Juan Pérez",
                        "role": "DOCTOR",
                        "password": "pass1"
                    }
                    """;

            User savedUser = buildUser(1L, "12345678-9", "Juan Pérez", UserRole.DOCTOR);
            UserResponseDTO responseDTO = buildResponseDTO(1L, "12345678-9", "Juan Pérez",
                    UserRole.DOCTOR, "Juan.1234");

            when(service.create(any(User.class))).thenReturn(new String[]{"1", "Juan.1234"});
            when(service.getByRut("12345678-9")).thenReturn(savedUser);

            try (MockedStatic<UserMapper> mapperMock = mockStatic(UserMapper.class)) {
                mapperMock.when(() -> UserMapper.toEntity(any())).thenReturn(savedUser);
                mapperMock.when(() -> UserMapper.toDTOWithPassword(savedUser, "Juan.1234"))
                          .thenReturn(responseDTO);

                mockMvc.perform(post("/users")
                               .with(csrf())
                               .contentType(MediaType.APPLICATION_JSON)
                               .content(body))
                       .andExpect(status().isOk())
                       .andExpect(jsonPath("$.rut").value("12345678-9"))
                       .andExpect(jsonPath("$.generatedPassword").value("Juan.1234"));
            }
        }
    }

    // ── GET /users ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /users")
    class ListUsers {

        @Test
        @WithMockUser
        @DisplayName("Retorna la lista de usuarios con status 200")
        void shouldReturnUserList() throws Exception {
            List<User> users = List.of(
                    buildUser(1L, "12345678-9", "Juan", UserRole.DOCTOR),
                    buildUser(2L, "98765432-1", "Ana",  UserRole.ADMIN)
            );

            when(service.getAllUsers()).thenReturn(users);

            mockMvc.perform(get("/users"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.length()").value(2))
                   .andExpect(jsonPath("$[0].rut").value("12345678-9"));
        }

        @Test
        @WithMockUser
        @DisplayName("Retorna lista vacía cuando no hay usuarios")
        void shouldReturnEmptyList() throws Exception {
            when(service.getAllUsers()).thenReturn(List.of());

            mockMvc.perform(get("/users"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ── GET /users/doctors ────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /users/doctors")
    class ListDoctors {

        @Test
        @WithMockUser
        @DisplayName("Retorna solo doctores activos")
        void shouldReturnOnlyActiveDoctors() throws Exception {
            User doctor      = buildUser(1L, "12345678-9", "Dr. Juan", UserRole.DOCTOR);
            User inactiveDoc = buildUser(2L, "11111111-1", "Dr. Inactivo", UserRole.DOCTOR);
            inactiveDoc.setActive(false);
            User admin = buildUser(3L, "98765432-1", "Admin", UserRole.ADMIN);

            when(service.getAllUsers()).thenReturn(List.of(doctor, inactiveDoc, admin));

            mockMvc.perform(get("/users/doctors"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.length()").value(1))
                   .andExpect(jsonPath("$[0].rut").value("12345678-9"));
        }
    }

    // ── GET /users/rut/{rut} ──────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /users/rut/{rut}")
    class GetByRut {

        @Test
        @WithMockUser
        @DisplayName("Retorna 200 con el usuario cuando el RUT existe")
        void shouldReturnUserWhenFound() throws Exception {
            User user = buildUser(1L, "12345678-9", "Juan", UserRole.DOCTOR);
            when(service.getByRut("12345678-9")).thenReturn(user);

            mockMvc.perform(get("/users/rut/12345678-9"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.rut").value("12345678-9"));
        }

        @Test
        @WithMockUser
        @DisplayName("Retorna 404 cuando el RUT no existe")
        void shouldReturn404WhenNotFound() throws Exception {
            when(service.getByRut("00000000-0"))
                    .thenThrow(new NotFoundException("Usuario no encontrado"));

            mockMvc.perform(get("/users/rut/00000000-0"))
                   .andExpect(status().isNotFound());
        }
    }

    // ── PUT /users/password ───────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /users/password")
    class UpdatePassword {

        @Test
        @WithMockUser
        @DisplayName("Actualiza contraseña y retorna 200")
        void shouldUpdatePasswordSuccessfully() throws Exception {
            PasswordUpdateDTO dto = new PasswordUpdateDTO();
            dto.setRut("12345678-9");
            dto.setNewPassword("nuevaPass123");

            doNothing().when(service).updatePassword("12345678-9", "nuevaPass123");

            mockMvc.perform(put("/users/password")
                           .with(csrf())
                           .contentType(MediaType.APPLICATION_JSON)
                           .content(objectMapper.writeValueAsString(dto)))
                   .andExpect(status().isOk())
                   .andExpect(content().string("Contraseña actualizada"));
        }

        @Test
        @WithMockUser
        @DisplayName("Retorna 404 si el RUT no existe al actualizar contraseña")
        void shouldReturn404WhenUserNotFound() throws Exception {
            PasswordUpdateDTO dto = new PasswordUpdateDTO();
            dto.setRut("99999999-9");
            dto.setNewPassword("pass");

            doThrow(new NotFoundException("Usuario no encontrado"))
                    .when(service).updatePassword("99999999-9", "pass");

            mockMvc.perform(put("/users/password")
                           .with(csrf())
                           .contentType(MediaType.APPLICATION_JSON)
                           .content(objectMapper.writeValueAsString(dto)))
                   .andExpect(status().isNotFound());
        }
    }

    // ── DELETE /users/{id} ────────────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /users/{id}")
    class DeleteUser {

        @Test
        @WithMockUser
        @DisplayName("Elimina usuario existente y retorna 200")
        void shouldDeleteUserSuccessfully() throws Exception {
            doNothing().when(service).deleteUser(1L);

            mockMvc.perform(delete("/users/1").with(csrf()))
                   .andExpect(status().isOk());

            verify(service).deleteUser(1L);
        }

        @Test
        @WithMockUser
        @DisplayName("Retorna 404 si el id no existe")
        void shouldReturn404WhenUserNotFound() throws Exception {
            doThrow(new NotFoundException("Usuario no encontrado"))
                    .when(service).deleteUser(99L);

            mockMvc.perform(delete("/users/99").with(csrf()))
                   .andExpect(status().isNotFound());
        }
    }
}