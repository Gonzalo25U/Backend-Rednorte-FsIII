package com.rednorte.user_service.utils;

public class RutValidator {

    public static boolean isValid(String rut) {
        if (rut == null || !rut.matches("\\d{7,8}-[0-9kK]")) {
            return false;
        }

        String[] parts = rut.split("-");
        String number = parts[0];
        char dv = parts[1].toLowerCase().charAt(0);

        int sum = 0;
        int multiplier = 2;

        for (int i = number.length() - 1; i >= 0; i--) {
            sum += Character.getNumericValue(number.charAt(i)) * multiplier;
            multiplier = (multiplier == 7) ? 2 : multiplier + 1;
        }

        int result = 11 - (sum % 11);

        char expectedDv;
        if (result == 11) expectedDv = '0';
        else if (result == 10) expectedDv = 'k';
        else expectedDv = Character.forDigit(result, 10);

        return dv == expectedDv;
    }
}