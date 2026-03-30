package com.rednorte.user_service.dto;

import com.rednorte.user_service.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponseDTO {

    private Long id;
    private String rut;
    private String name;
    private UserRole role;
    private boolean active;
}