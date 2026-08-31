package com.bike_assembly.integracion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void PI01_sinSesionLaRaizCargaLaVistaPrincipal() throws Exception {
        // Hallazgo real: la raiz "/" es publica (permitAll) -> responde 200 con la vista "inicio"
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("inicio"));
    }

    @Test
    void PI02_sinSesionElLoginCargaLaVistaLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("/login"));
    }

    @Test
    void PI03_conSesionLaRaizCargaLaVistaPrincipal() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void PI04_usuarioConRolAdminEsRedirigidoDesdeRutaAdmin() throws Exception {
        // Hallazgo real: "/admin" no tiene GET propio (acceso via "/admin/..."); responde 302
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void PI05_rutaProtegidaRechazaUsuarioSinRolAdmin() throws Exception {
        mockMvc.perform(get("/admin/proveedor"))
                .andExpect(status().is3xxRedirection());
    }
}
