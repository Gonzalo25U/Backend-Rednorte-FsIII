package com.rednorte.user_service.dto;

import com.rednorte.user_service.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // no muestra campos null en el JSON
public class UserResponseDTO {

    private Long id;
    private String rut;
    private String name;
    private UserRole role;
    private boolean active;
    private String generatedPassword; // solo aparece al crear, null en el resto
}