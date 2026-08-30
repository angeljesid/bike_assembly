package com.bike_assembly.security;

import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.UsuarioDao;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class UsuarioActivoFilter extends OncePerRequestFilter {

    private final UsuarioDao usuarioDao;

    public UsuarioActivoFilter(UsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Usuario usuario = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);
            boolean activo = usuario != null && (usuario.getActivo() == null || usuario.getActivo());
            if (!activo) {
                SecurityContextHolder.clearContext();
                request.getSession().invalidate();
                response.sendRedirect("/login");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
