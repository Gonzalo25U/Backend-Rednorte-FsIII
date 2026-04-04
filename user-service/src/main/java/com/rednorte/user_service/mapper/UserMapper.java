package com.rednorte.user_service.mapper;

import com.rednorte.user_service.dto.UserRequestDTO;
import com.rednorte.user_service.dto.UserResponseDTO;
import com.rednorte.user_service.model.User;

public class UserMapper {

    public static User toEntity(UserRequestDTO dto) {
        User user = new User();

        user.setRut(dto.getRut());
        user.setName(dto.getName());
        user.setPassword(dto.getPassword());

        // ✅ ahora el DTO ya trae el enum validado
        user.setRole(dto.getRole());

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