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

    //  Crear usuario con validaciones
    public User create(User user) {


        //  Validar duplicado
        if (repository.findByRut(user.getRut()).isPresent()) {
            throw new BadRequestException("El RUT ya está registrado");
        }

        //  Validar ADMIN único
        if (user.getRole() == UserRole.ADMIN) {
            boolean existsAdmin = repository.existsByRole(UserRole.ADMIN);
            if (existsAdmin) {
                throw new BadRequestException("Ya existe un ADMIN");
            }
        }

        //  Generar contraseña automática (PLANO)
        String generatedPassword = PasswordGenerator.generate();

        //  Encriptar con BCrypt
        String encodedPassword = passwordEncoder.encode(generatedPassword);

        user.setPassword(encodedPassword);

        //  IMPORTANTE: podrías logear o devolver la password generada al admin
        System.out.println("Contraseña generada (mostrar solo una vez): " + generatedPassword);

        return repository.save(user);
        }

        //  Listar todos
        public List<User> getAllUsers() {
            return repository.findAll();
        }

        //  Buscar por RUT
        public User getByRut(String rut) {
            return repository.findByRut(rut)
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        }

        //  Eliminar usuario
        public void deleteUser(Long id) {

            if (!repository.existsById(id)) {
                throw new NotFoundException("Usuario no encontrado");
            }

            repository.deleteById(id);
        }
}