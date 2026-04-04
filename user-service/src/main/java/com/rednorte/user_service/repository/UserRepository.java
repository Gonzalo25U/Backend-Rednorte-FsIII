package com.rednorte.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rednorte.user_service.enums.UserRole;
import com.rednorte.user_service.model.User;


public interface UserRepository extends JpaRepository<User, Long> {

    // buscar por rut
    Optional<User> findByRut(String rut);

    // validar si existe un ADMIN
    boolean existsByRole(UserRole role); 
}