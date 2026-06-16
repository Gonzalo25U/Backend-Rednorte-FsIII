package com.rednorte.user_service.dto;

import com.rednorte.user_service.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponseDTO {

    private Long id;
    private String rut;
    private String name;
    private UserRole role;
    private boolean active;
    private String generatedPassword;
}