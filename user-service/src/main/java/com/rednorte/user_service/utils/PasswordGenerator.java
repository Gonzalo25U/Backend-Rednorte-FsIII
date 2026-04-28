package com.rednorte.user_service.utils;

public class PasswordGenerator {

    public static String generate(String name, String rut) {
        // Tomar primer caracter del nombre (mayúscula)
        String namePart = name != null && !name.isEmpty()
                ? String.valueOf(name.charAt(0)).toUpperCase()
                : "X";

        // Tomar últimos 2 dígitos del RUT (sin guión ni dígito verificador)
        String cleanRut = rut.replaceAll("[^0-9]", "");
        String rutPart = cleanRut.length() >= 2
                ? cleanRut.substring(cleanRut.length() - 2)
                : cleanRut;

        // Tomar último caracter del nombre (minúscula)
        String nameLast = name != null && name.length() > 1
                ? String.valueOf(name.charAt(name.length() - 1)).toLowerCase()
                : "x";

        // Resultado: 4 caracteres, ej: "T22o" para nombre "Test" y rut "22222222-2"
        return namePart + rutPart + nameLast;
    }
}