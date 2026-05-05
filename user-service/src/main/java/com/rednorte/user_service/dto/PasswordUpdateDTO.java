package com.rednorte.user_service.dto;
 
import lombok.Data;
 
@Data
public class PasswordUpdateDTO {
    private String rut;
    private String newPassword;
}
 