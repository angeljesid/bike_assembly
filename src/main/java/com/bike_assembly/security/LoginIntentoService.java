package com.bike_assembly.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protección contra fuerza bruta: registra intentos fallidos de login por nombre de
 * usuario y bloquea de forma temporal tras un máximo de intentos en una ventana de tiempo.
 * Almacenamiento en memoria (compatible con despliegue de un solo nodo).
 */
@Component
public class LoginIntentoService {

    private static final int MAX_INTENTOS = 5;
    private static final Duration VENTANA = Duration.ofMinutes(15);

    private static class Registro {
        int intentos;
        Instant primerFallo;
        Instant bloqueadoHasta;
    }

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();

    public boolean isBloqueado(String username) {
        if (username == null) {
            return false;
        }
        Registro r = registros.get(normalizar(username));
        if (r == null) {
            return false;
        }
        if (r.bloqueadoHasta != null && Instant.now().isAfter(r.bloqueadoHasta)) {
            registros.remove(normalizar(username));
            return false;
        }
        return r.bloqueadoHasta != null;
    }

    public void registrarFallo(String username) {
        if (username == null) {
            return;
        }
        String key = normalizar(username);
        Registro r = registros.computeIfAbsent(key, k -> new Registro());
        Instant ahora = Instant.now();
        if (r.primerFallo == null || Duration.between(r.primerFallo, ahora).compareTo(VENTANA) > 0) {
            // Nueva ventana
            r.primerFallo = ahora;
            r.intentos = 0;
            r.bloqueadoHasta = null;
        }
        r.intentos++;
        if (r.intentos >= MAX_INTENTOS) {
            r.bloqueadoHasta = ahora.plus(VENTANA);
        }
    }

    public void limpiar(String username) {
        if (username != null) {
            registros.remove(normalizar(username));
        }
    }

    private String normalizar(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }
}
