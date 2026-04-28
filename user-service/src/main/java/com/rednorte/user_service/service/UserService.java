package com.rednorte.user_service.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rednorte.user_service.utils.RutValidator;
import com.rednorte.user_service.exception.BadRequestException;
import com.rednorte.user_service.exception.NotFoundException;
import com.rednorte.user_service.enums.UserRole;
import com.rednorte.user_service.model.User;
import com.rednorte.user_service.repository.UserRepository;
import com.rednorte.user_service.utils.PasswordGenerator;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    // Crear usuario — devuelve la contraseña generada en texto plano
    public String[] create(User user) {

        if (repository.findByRut(user.getRut()).isPresent()) {
            throw new BadRequestException("El RUT ya está registrado");
        }

        if (user.getRole() == UserRole.ADMIN) {
            boolean existsAdmin = repository.existsByRole(UserRole.ADMIN);
            if (existsAdmin) {
                throw new BadRequestException("Ya existe un ADMIN");
            }
        }

        // Generar contraseña de 4 caracteres basada en nombre y RUT
        String generatedPassword = PasswordGenerator.generate(user.getName(), user.getRut());

        // Encriptar y guardar
        user.setPassword(passwordEncoder.encode(generatedPassword));

        User saved = repository.save(user);

        // Devolver [id, generatedPassword] para que el controller arme el DTO
        return new String[]{String.valueOf(saved.getId()), generatedPassword};
    }

    // Editar contraseña
    public void updatePassword(String rut, String newPassword) {
        User user = repository.findByRut(rut)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        user.setPassword(passwordEncoder.encode(newPassword));
        repository.save(user);
    }

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    public User getByRut(String rut) {
        return repository.findByRut(rut)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    public void deleteUser(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Usuario no encontrado");
        }
        repository.deleteById(id);
    }
}