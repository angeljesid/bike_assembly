package com.bike_assembly.security;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginIntentoServiceTest {

    private final LoginIntentoService servicio = new LoginIntentoService();

    @Test
    void usuarioNullNuncaBloquea() {
        assertFalse(servicio.isBloqueado(null));
        servicio.registrarFallo(null);
        assertFalse(servicio.isBloqueado("cualquiera"));
    }

    @Test
    void conMenosDeCincoFallosNoBloquea() {
        for (int i = 0; i < 4; i++) {
            servicio.registrarFallo("ADMIN");
        }
        assertFalse(servicio.isBloqueado("admin"));
    }

    @Test
    void conCincoFallosBloquea() {
        for (int i = 0; i < 5; i++) {
            servicio.registrarFallo("Admin ");
        }
        assertTrue(servicio.isBloqueado(" admin"));
    }

    @Test
    void limpiarDesbloquea() {
        for (int i = 0; i < 5; i++) {
            servicio.registrarFallo("juan");
        }
        assertTrue(servicio.isBloqueado("juan"));

        servicio.limpiar("juan");

        assertFalse(servicio.isBloqueado("juan"));
    }

    @Test
    void trasBloqueoExpiradoSeDesbloquea() throws Exception {
        for (int i = 0; i < 5; i++) {
            servicio.registrarFallo("juan");
        }
        assertTrue(servicio.isBloqueado("juan"));

        fijarCampo("juan", "bloqueadoHasta", Instant.now().minusSeconds(60));

        assertFalse(servicio.isBloqueado("juan"));
    }

    @Test
    void unaVentanaNuevaReiniciaElConteo() throws Exception {
        for (int i = 0; i < 5; i++) {
            servicio.registrarFallo("juan");
        }

        fijarCampo("juan", "primerFallo", Instant.now().minusSeconds(16 * 60));

        servicio.registrarFallo("juan");

        assertFalse(servicio.isBloqueado("juan"));
    }

    @SuppressWarnings("unchecked")
    private Object registroDe(String username) throws Exception {
        Field campo = LoginIntentoService.class.getDeclaredField("registros");
        campo.setAccessible(true);
        Map<String, Object> registros = (Map<String, Object>) campo.get(servicio);
        return registros.get(username.trim().toLowerCase());
    }

    private void fijarCampo(String username, String campoNombre, Instant valor) throws Exception {
        Object registro = registroDe(username);
        Field campo = registro.getClass().getDeclaredField(campoNombre);
        campo.setAccessible(true);
        campo.set(registro, valor);
    }
}