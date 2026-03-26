package com.rednorte.user_service.dto;

import lombok.Data;

@Data
public class UserResponseDTO {

    private Long id;
    private String rut;
    private String name;
    private String role;
    private boolean active;

    public UserResponseDTO(Long id, String rut, String name, String role, boolean active) {
        this.id = id;
        this.rut = rut;
        this.name = name;
        this.role = role;
        this.active = active;
    }

}