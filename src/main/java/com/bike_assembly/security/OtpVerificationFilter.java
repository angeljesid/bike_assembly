package com.bike_assembly.security;

import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.services.usuario.OtpService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Obliga a que los usuarios con 2FA (admin, superadmin, caja, operario) completen
 * la verificación OTP de la sesión antes de acceder a sus áreas autenticadas.
 * Se registra como bean en SecurityConfig (no @Component) para evitar doble filtrado.
 */
public class OtpVerificationFilter extends OncePerRequestFilter {

    @Autowired
    private OtpService otpService;

    @Autowired
    private UsuarioDao usuarioDao;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/verificar-otp")
                || path.startsWith("/login")
                || path.equals("/")
                || path.startsWith("/css")
                || path.startsWith("/js")
                || path.startsWith("/images")
                || path.startsWith("/recursos")
                || path.startsWith("/registro")
                || path.startsWith("/recuperar-password")
                || path.startsWith("/bloqueado");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            boolean otpVerificado = Boolean.TRUE.equals(request.getSession().getAttribute(AtributosSesion.OTP_VERIFICADO));

            if (!otpVerificado) {
                Usuario usuario = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);
                if (usuario != null && otpService.requiereOtp(usuario.getRol())) {
                    response.sendRedirect("/verificar-otp");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
