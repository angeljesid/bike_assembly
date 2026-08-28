package com.bike_assembly.security;

import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.UsuarioDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Autowired
    private UsuarioDao usuarioDao;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        if (exception instanceof DisabledException) {
            String username = request.getParameter("username");
            String rol = "";
            if (username != null) {
                Usuario usuario = usuarioDao.findByUsername(username).orElse(null);
                if (usuario != null && usuario.getRol() != null) {
                    rol = usuario.getRol().name();
                }
            }
            if (Rol.ADMIN.name().equals(rol)) {
                response.sendRedirect("/bloqueado/admin");
            } else if (Rol.OPERARIO.name().equals(rol)) {
                response.sendRedirect("/bloqueado/operario");
            } else if (Rol.CAJA.name().equals(rol)) {
                response.sendRedirect("/bloqueado/caja");
            } else {
                response.sendRedirect("/bloqueado");
            }
        } else {
            response.sendRedirect("/login?error");
        }
    }
}
