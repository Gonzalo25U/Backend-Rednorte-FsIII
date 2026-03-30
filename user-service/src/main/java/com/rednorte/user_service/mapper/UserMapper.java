package com.rednorte.user_service.mapper;

import com.rednorte.user_service.dto.UserRequestDTO;
import com.rednorte.user_service.dto.UserResponseDTO;
import com.rednorte.user_service.enums.UserRole;
import com.rednorte.user_service.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserMapper {

    public static User toEntity(UserRequestDTO dto) {
        User user = new User();

        user.setRut(dto.getRut());
        user.setName(dto.getName());
        user.setPassword(dto.getPassword());

        try {
            user.setRole(UserRole.valueOf(dto.getRole().toUpperCase()));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido");
        }

        return user;
    }

    public static UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getRut(),
                user.getName(),
                user.getRole(),
                user.isActive()
        );
    }
}