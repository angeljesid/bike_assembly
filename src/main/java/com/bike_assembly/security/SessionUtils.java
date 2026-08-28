package com.bike_assembly.security;

import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.UsuarioDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SessionUtils {

    @Autowired
    private UsuarioDao usuarioDao;

    public Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);
    }
}
