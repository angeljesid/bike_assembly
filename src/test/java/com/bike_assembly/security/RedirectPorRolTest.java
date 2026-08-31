package com.bike_assembly.security;

import com.bike_assembly.domain.Rol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RedirectPorRolTest {

    @Test
    void superAdminVaAlDashboard() {
        assertEquals("/superadmin/dashboard", RedirectPorRol.urlDestino(Rol.SUPER_ADMIN));
    }

    @Test
    void adminVaAlPanelPrincipal() {
        assertEquals("/admin", RedirectPorRol.urlDestino(Rol.ADMIN));
    }

    @Test
    void cajaVaAlPuntoDeVenta() {
        assertEquals("/caja/pos", RedirectPorRol.urlDestino(Rol.CAJA));
    }

    @Test
    void operarioVaAlModuloDeEnsambles() {
        assertEquals("/operario/ensambles", RedirectPorRol.urlDestino(Rol.OPERARIO));
    }

    @Test
    void clienteVaAlCatalogo() {
        assertEquals("/cliente/catalogo", RedirectPorRol.urlDestino(Rol.CLIENTE));
    }

    @Test
    void rolNullVaALaRaiz() {
        assertEquals("/", RedirectPorRol.urlDestino(null));
    }
}