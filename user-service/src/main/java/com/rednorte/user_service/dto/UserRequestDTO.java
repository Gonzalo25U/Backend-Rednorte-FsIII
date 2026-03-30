package com.rednorte.user_service.dto;



import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class UserRequestDTO {

    @NotBlank
    private String rut;

    @NotBlank
    private String name;

    @NotBlank
    private String role;

    @NotBlank
    private String password;

}