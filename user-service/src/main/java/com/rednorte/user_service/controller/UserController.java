package com.rednorte.user_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rednorte.user_service.dto.PasswordUpdateDTO;
import com.rednorte.user_service.dto.UserRequestDTO;
import com.rednorte.user_service.dto.UserResponseDTO;
import com.rednorte.user_service.enums.UserRole;
import com.rednorte.user_service.mapper.UserMapper;
import com.rednorte.user_service.model.User;
import com.rednorte.user_service.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public UserResponseDTO create(@RequestBody UserRequestDTO dto) {
        User user = UserMapper.toEntity(dto);
        String[] result = service.create(user);
        String generatedPassword = result[1];
        User saved = service.getByRut(user.getRut());
        return UserMapper.toDTOWithPassword(saved, generatedPassword);
    }

    @GetMapping
    public List<User> list() {
        return service.getAllUsers();
    }

    // Endpoint interno para obtener solo doctores activos - usado por el BFF
    @GetMapping("/doctors")
    public List<User> listDoctors() {
        return service.getAllUsers().stream()
                .filter(u -> u.getRole() == UserRole.DOCTOR && u.isActive())
                .toList();
    }

    @GetMapping("/rut/{rut}")
    public ResponseEntity<?> getByRut(@PathVariable String rut) {
        User user = service.getByRut(rut);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordUpdateDTO dto) {
        service.updatePassword(dto.getRut(), dto.getNewPassword());
        return ResponseEntity.ok("Contraseña actualizada");
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteUser(id);
    }
}