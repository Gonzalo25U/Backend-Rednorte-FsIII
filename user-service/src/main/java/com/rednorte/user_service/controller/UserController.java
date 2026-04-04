package com.rednorte.user_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.rednorte.user_service.dto.UserRequestDTO;
import com.rednorte.user_service.dto.UserResponseDTO;
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
    public UserResponseDTO create(
            @RequestBody UserRequestDTO dto,
            @RequestHeader("role") String role
    ) {
        if (!role.equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo ADMIN puede crear usuarios");
        }

        User user = UserMapper.toEntity(dto);
        return UserMapper.toDTO(service.create(user));
    }

    @GetMapping
    public List<User> list() {
        return service.getAllUsers();
    }

    @GetMapping("/{rut}")
    public ResponseEntity<?> getByRut(@PathVariable String rut) {
        User user = service.getByRut(rut);

        if (user == null) {
            return ResponseEntity.notFound().build(); // ✅ 404 correcto
        }

        return ResponseEntity.ok(user);
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteUser(id);
    }
}
