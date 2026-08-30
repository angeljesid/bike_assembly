package com.bike_assembly.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 1) Bloquea que el navegador cachee las páginas (evita que los botones atrás/adelante
 *    muestren vistas autenticadas desde caché tras salir).
 * 2) Si el usuario navega al login o a la portada con sesión activa, cierra la sesión
 *    (así no queda "logueado" al usar atrás/adelante del navegador).
 * El filtro se registra como bean en SecurityConfig (no como @Component) para
 * evitar que Spring Boot lo registre además como filtro de contenedor.
 */
public class NoCacheFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Cerrar sesión al visitar login o portada (GET) si hay sesión autenticada activa.
        // Se limita a GET para no interrumpir el POST de login ni la verificación OTP.
        boolean esPaginaPublica = path.equals("/") || path.equals("/login");
        if ("GET".equalsIgnoreCase(request.getMethod()) && esPaginaPublica) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean autenticado = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
            if (autenticado) {
                SecurityContextHolder.clearContext();
                jakarta.servlet.http.HttpSession sesion = request.getSession(false);
                if (sesion != null) {
                    sesion.invalidate();
                }
            }
        }

        // Bloquear caché en todas las respuestas
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        filterChain.doFilter(request, response);
    }
}
