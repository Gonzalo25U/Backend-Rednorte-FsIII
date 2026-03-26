package com.rednorte.user_service.service;

import org.springframework.stereotype.Service;

import com.rednorte.user_service.model.User;
import com.rednorte.user_service.repository.UserRepository;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User createUser(User user) {
        if (repository.findByRut(user.getRut()).isPresent()) {
            throw new RuntimeException("El usuario ya existe");
        }
        return repository.save(user);
    }

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    public User getByRut(String rut) {
        return repository.findByRut(rut)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public void deleteUser(Long id) {
        repository.deleteById(id);
    }
}