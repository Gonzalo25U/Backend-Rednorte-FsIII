package com.rednorte.auth_service.client;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String rut;
    private String name;
    private String role;
    private boolean active;
    private String password;
}