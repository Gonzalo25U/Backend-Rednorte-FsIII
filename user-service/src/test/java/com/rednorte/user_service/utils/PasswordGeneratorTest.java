package com.rednorte.user_service.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordGeneratorTest {

    @Nested
    @DisplayName("generate()")
    class Generate {

        @Test
        @DisplayName("Genera contraseña con formato correcto")
        void shouldGeneratePasswordWithCorrectFormat() {
            String password = PasswordGenerator.generate("Test", "22222222-2");
            assertThat(password).isEqualTo("T22t");
        }

        @Test
        @DisplayName("Primera letra del nombre en mayúscula")
        void shouldCapitalizeFirstLetter() {
            String password = PasswordGenerator.generate("juan", "12345678-9");
            assertThat(password).startsWith("J");
        }

        @Test
        @DisplayName("Última letra del nombre en minúscula")
        void shouldLowercaseLastLetter() {
            String password = PasswordGenerator.generate("Juan", "12345678-9");
            assertThat(password).endsWith("n");
        }

        @Test
        @DisplayName("Incluye los últimos 2 dígitos del RUT")
        void shouldIncludeLast2DigitsOfRut() {
            String password = PasswordGenerator.generate("Ana", "77777777-7");
            assertThat(password).contains("77");
        }

        @Test
        @DisplayName("Maneja nombre null con valor por defecto")
        void shouldHandleNullName() {
            String password = PasswordGenerator.generate(null, "12345678-9");
            assertThat(password).startsWith("X");
            assertThat(password).endsWith("x");
        }

        @Test
        @DisplayName("Maneja nombre vacío con valor por defecto")
        void shouldHandleEmptyName() {
            String password = PasswordGenerator.generate("", "12345678-9");
            assertThat(password).startsWith("X");
        }

        @Test
        @DisplayName("Maneja nombre de un solo carácter")
        void shouldHandleSingleCharName() {
            String password = PasswordGenerator.generate("A", "12345678-9");
            assertThat(password).startsWith("A");
            assertThat(password).endsWith("x");
        }

        @Test
        @DisplayName("Genera contraseña de exactamente 4 caracteres")
        void shouldGenerateFourCharPassword() {
            String password = PasswordGenerator.generate("Test", "12345678-9");
            assertThat(password).hasSize(4);
        }

        @Test
        @DisplayName("Ignora caracteres no numéricos del RUT correctamente")
        void shouldIgnoreNonNumericCharsInRut() {
            String password = PasswordGenerator.generate("Test", "77777777-7");
            assertThat(password).contains("77");
        }
    }
}