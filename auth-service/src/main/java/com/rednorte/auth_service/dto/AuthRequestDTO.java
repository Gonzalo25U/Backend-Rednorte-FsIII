package com.rednorte.auth_service.dto;

import lombok.Data;

@Data
public class AuthRequestDTO {
    private String rut;
    private String password;
}