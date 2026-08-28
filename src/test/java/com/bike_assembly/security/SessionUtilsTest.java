package com.bike_assembly.security;

import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.UsuarioDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionUtilsTest {

    @Mock
    private UsuarioDao usuarioDao;

    @InjectMocks
    private SessionUtils sessionUtils;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sinContextoDevuelveNull() {
        assertNull(sessionUtils.usuarioActual());
    }

    @Test
    void conUsuarioAutenticadoDevuelveElUsuario() {
        Usuario esperado = new Usuario();
        esperado.setUsername("admin");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "x", List.of()));

        when(usuarioDao.findByUsernameOrEmail("admin", "admin")).thenReturn(Optional.of(esperado));

        Usuario resultado = sessionUtils.usuarioActual();

        assertEquals("admin", resultado.getUsername());
    }
}
