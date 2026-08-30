package com.bike_assembly.security;

import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.services.usuario.OtpService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private OtpService otpService;

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private LoginIntentoService loginIntentoService;

    @Value("${app.otp.desactivado:false}")
    private boolean otpDesactivado;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Rol rol = obtenerRol(authentication.getAuthorities());
        Usuario usuario = usuarioDao.findByUsernameOrEmail(authentication.getName(), authentication.getName())
                .orElse(null);

        // Login exitoso: se limpian los intentos fallidos previos
        loginIntentoService.limpiar(authentication.getName());

        String destino = RedirectPorRol.urlDestino(rol);

        if (!otpDesactivado && usuario != null && otpService.requiereOtp(rol)) {
            // El usuario debe pasar por el 2FA antes de entrar a su área
            request.getSession().setAttribute("urlDestino", destino);
            response.sendRedirect("/verificar-otp");
            return;
        }

        // Con 2FA desactivado (o rol sin OTP) se marca la sesión como verificada
        request.getSession().setAttribute(AtributosSesion.OTP_VERIFICADO, Boolean.TRUE);
        response.sendRedirect(destino);
    }

    private Rol obtenerRol(Collection<? extends GrantedAuthority> authorities) {
        for (GrantedAuthority authority : authorities) {
            String auth = authority.getAuthority();
            if (auth != null && auth.startsWith("ROLE_")) {
                try {
                    return Rol.valueOf(auth.substring(5));
                } catch (IllegalArgumentException ignored) {
                    // rol desconocido
                }
            }
        }
        return null;
    }
}
