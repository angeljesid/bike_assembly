package com.bike_assembly.services;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Valida que una contraseña cumpla la política de seguridad fuerte:
 * mínimo 8 caracteres, al menos una mayúscula, una minúscula, un número y un símbolo.
 */
public final class PasswordValidator {

    private static final int MIN_LONGITUD = 8;
    private static final Pattern MAYUSCULA = Pattern.compile("[A-Z]");
    private static final Pattern MINUSCULA = Pattern.compile("[a-z]");
    private static final Pattern NUMERO = Pattern.compile("[0-9]");
    private static final Pattern SIMBOLO = Pattern.compile("[^A-Za-z0-9]");

    private PasswordValidator() {
    }

    /**
     * Devuelve el primer mensaje de error si la contraseña no cumple la política,
     * o null si es válida.
     */
    public static String validar(String password) {
        if (password == null) {
            return "La contraseña es obligatoria.";
        }
        List<String> errores = new ArrayList<>();
        if (password.length() < MIN_LONGITUD) {
            errores.add("debe tener al menos " + MIN_LONGITUD + " caracteres");
        }
        if (!MAYUSCULA.matcher(password).find()) {
            errores.add("debe incluir al menos una letra mayúscula");
        }
        if (!MINUSCULA.matcher(password).find()) {
            errores.add("debe incluir al menos una letra minúscula");
        }
        if (!NUMERO.matcher(password).find()) {
            errores.add("debe incluir al menos un número");
        }
        if (!SIMBOLO.matcher(password).find()) {
            errores.add("debe incluir al menos un símbolo");
        }
        if (errores.isEmpty()) {
            return null;
        }
        return "La contraseña " + String.join(", ", errores) + ".";
    }
}
