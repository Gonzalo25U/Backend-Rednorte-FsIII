package com.rednorte.user_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public User create(@RequestBody User user) {
        return service.createUser(user);
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
