package com.rednorte.user_service.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rednorte.user_service.dto.UserResponseDTO;
import com.rednorte.user_service.exception.BadRequestException;
import com.rednorte.user_service.exception.NotFoundException;
import com.rednorte.user_service.enums.UserRole;
import com.rednorte.user_service.mapper.UserMapper;
import com.rednorte.user_service.model.User;
import com.rednorte.user_service.repository.UserRepository;
import com.rednorte.user_service.utils.PasswordGenerator;
import com.rednorte.user_service.utils.RutValidator;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @CacheEvict(value = "doctors", allEntries = true)
    public String[] create(User user) {
        if (!RutValidator.isValid(user.getRut())) {
            throw new BadRequestException("RUT inválido. Formato requerido: xxxxxxxx-x");
        }

        if (repository.findByRut(user.getRut()).isPresent()) {
            throw new BadRequestException("El RUT ya está registrado");
        }

        if (user.getRole() == UserRole.ADMIN) {
            boolean existsAdmin = repository.existsByRole(UserRole.ADMIN);
            if (existsAdmin) {
                throw new BadRequestException("Ya existe un ADMIN");
            }
        }

        String generatedPassword = PasswordGenerator.generate(user.getName(), user.getRut());
        user.setPassword(passwordEncoder.encode(generatedPassword));

        User saved = repository.save(user);

        return new String[]{String.valueOf(saved.getId()), generatedPassword};
    }

    public void updatePassword(String rut, String newPassword) {
        User user = repository.findByRut(rut)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        user.setPassword(passwordEncoder.encode(newPassword));
        repository.save(user);
    }

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    @Cacheable(value = "doctors")
    public List<UserResponseDTO> getDoctors() {
        return repository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.DOCTOR && u.isActive())
                .map(UserMapper::toDTO)
                .toList();
    }

    public User getByRut(String rut) {
        return repository.findByRut(rut)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    @CacheEvict(value = "doctors", allEntries = true)
    public void deleteUser(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Usuario no encontrado");
        }
        repository.deleteById(id);
    }
}