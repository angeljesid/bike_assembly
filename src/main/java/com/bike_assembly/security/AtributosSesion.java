package com.bike_assembly.security;

public final class AtributosSesion {

    private AtributosSesion() {
    }

    /** Marca que el usuario ya completó la verificación 2FA de hoy en esta sesión. */
    public static final String OTP_VERIFICADO = "otpVerificado";
}
