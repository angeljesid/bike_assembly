package com.bike_assembly.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            
            if (role.equals("ROLE_SUPER_ADMIN")) {
                response.sendRedirect("/superadmin/dashboard");
                return;
            } else if (role.equals("ROLE_ADMIN")) {
                response.sendRedirect("/admin");
                return;
            } else if (role.equals("ROLE_CAJA")) {
                response.sendRedirect("/caja/pos");
                return;
            } else if (role.equals("ROLE_OPERARIO")) {
                response.sendRedirect("/operario/ensambles");
                return;
            } else if (role.equals("ROLE_CLIENTE")) {
                response.sendRedirect("/cliente/catalogo");
                return;
            }
        }

        response.sendRedirect("/");
    }
}