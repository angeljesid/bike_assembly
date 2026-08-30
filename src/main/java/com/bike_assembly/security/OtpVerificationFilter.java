package com.bike_assembly.security;

import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.services.usuario.OtpService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class OtpVerificationFilter extends OncePerRequestFilter {

    private final OtpService otpService;
    private final UsuarioDao usuarioDao;

    @org.springframework.beans.factory.annotation.Value("${app.otp.desactivado:false}")
    private boolean otpDesactivado;

    public OtpVerificationFilter(OtpService otpService, UsuarioDao usuarioDao) {
        this.otpService = otpService;
        this.usuarioDao = usuarioDao;
    }

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

        // Si el 2FA está desactivado por configuración, no se exige verificación OTP.
        if (otpDesactivado) {
            filterChain.doFilter(request, response);
            return;
        }

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
