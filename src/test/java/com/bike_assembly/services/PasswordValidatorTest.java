package com.bike_assembly.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordValidatorTest {

    @Test
    void passwordValidaNoDevuelveErrores() {
        assertNull(PasswordValidator.validar("Bike123!"));
    }

    @Test
    void passwordNullEsObligatoria() {
        assertEquals("La contraseña es obligatoria.", PasswordValidator.validar(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"corta1!", "SINMINUSCULA1!", "sinmayuscula1!", "SinSimbolos1", "Bike123"})
    void passwordInvalidaDevuelveError(String password) {
        assertNotNull(PasswordValidator.validar(password));
    }

    @Test
    void passwordCortaReportaTodasLasReglasFaltantes() {
        String error = PasswordValidator.validar("abcdef");

        assertNotNull(error);
        assertTrue(error.contains("8 caracteres"));
        assertTrue(error.contains("mayúscula"));
        assertTrue(error.contains("número"));
        assertTrue(error.contains("símbolo"));
    }
}