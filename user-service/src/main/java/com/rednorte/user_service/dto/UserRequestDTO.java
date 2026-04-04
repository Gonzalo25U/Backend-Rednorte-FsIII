package com.rednorte.user_service.dto;



import com.rednorte.user_service.enums.UserRole;
import jakarta.validation.constraints.*;

import lombok.Data;

@Data
public class UserRequestDTO {

    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(
        regexp = "\\d{7,8}-[0-9kK]",
        message = "Formato de RUT inválido (ej: 12345678-9)"
    )
    private String rut;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
    private String name;

    @NotNull(message = "El rol es obligatorio")
    private UserRole role;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 4, message = "La contraseña debe tener al menos 4 caracteres")
    private String password;
}