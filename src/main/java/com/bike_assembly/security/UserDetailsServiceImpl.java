package com.bike_assembly.security;

import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.UsuarioDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioDao usuarioDao;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Busca si el dato ingresado es un Email o un Username
        Usuario usuario = usuarioDao.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario o correo no encontrado: " + usernameOrEmail));

        // Asigna el rol con el prefijo ROLE_ (requerido por Spring Security)
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name());

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                Collections.singletonList(authority)
        );
    }
}