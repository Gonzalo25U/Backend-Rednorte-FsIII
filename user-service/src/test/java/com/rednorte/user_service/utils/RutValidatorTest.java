package com.rednorte.user_service.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RutValidatorTest {

    @Nested
    @DisplayName("RUTs válidos")
    class ValidRuts {

        @Test
        @DisplayName("RUT válido 11111111-1")
        void shouldReturnTrueForValidRut() {
            assertThat(RutValidator.isValid("11111111-1")).isTrue();
        }

        @Test
        @DisplayName("RUT válido 77777777-7")
        void shouldReturnTrueForAnotherValidRut() {
            assertThat(RutValidator.isValid("77777777-7")).isTrue();
        }

        @Test
        @DisplayName("RUT válido 20064625-8")
        void shouldReturnTrueForRutWithEightDv() {
            assertThat(RutValidator.isValid("20064625-8")).isTrue();
        }

        @Test
        @DisplayName("RUT válido 33333333-3")
        void shouldReturnTrueForRutThrees() {
            assertThat(RutValidator.isValid("33333333-3")).isTrue();
        }

        @Test
        @DisplayName("RUT válido 22222222-2")
        void shouldReturnTrueForRutTwos() {
            assertThat(RutValidator.isValid("22222222-2")).isTrue();
        }
    }

    @Nested
    @DisplayName("RUTs inválidos")
    class InvalidRuts {

        @Test
        @DisplayName("Retorna false para null")
        void shouldReturnFalseForNull() {
            assertThat(RutValidator.isValid(null)).isFalse();
        }

        @Test
        @DisplayName("Retorna false para string vacío")
        void shouldReturnFalseForEmptyString() {
            assertThat(RutValidator.isValid("")).isFalse();
        }

        @Test
        @DisplayName("Retorna false para RUT sin guión")
        void shouldReturnFalseForRutWithoutDash() {
            assertThat(RutValidator.isValid("123456789")).isFalse();
        }

        @Test
        @DisplayName("Retorna false para RUT con dígito verificador incorrecto")
        void shouldReturnFalseForWrongDv() {
            assertThat(RutValidator.isValid("11111111-2")).isFalse();
        }

        @Test
        @DisplayName("Retorna false para RUT con letras en el número")
        void shouldReturnFalseForRutWithLetters() {
            assertThat(RutValidator.isValid("1234abc8-9")).isFalse();
        }

        @Test
        @DisplayName("Retorna false para RUT con menos de 7 dígitos")
        void shouldReturnFalseForShortRut() {
            assertThat(RutValidator.isValid("12345-9")).isFalse();
        }

        @Test
        @DisplayName("Retorna false para RUT con dígito verificador inválido")
        void shouldReturnFalseForInvalidDvChar() {
            assertThat(RutValidator.isValid("11111111-X")).isFalse();
        }

        @Test
        @DisplayName("Retorna false para formato con espacios")
        void shouldReturnFalseForRutWithSpaces() {
            assertThat(RutValidator.isValid("11111111 1")).isFalse();
        }

        @Test
        @DisplayName("Retorna false para RUT con DV correcto pero número alterado")
        void shouldReturnFalseForAlteredNumber() {
            assertThat(RutValidator.isValid("11111112-1")).isFalse();
        }
    }
}