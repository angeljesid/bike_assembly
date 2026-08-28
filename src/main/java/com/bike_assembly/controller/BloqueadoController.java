package com.bike_assembly.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BloqueadoController {

    @GetMapping("/bloqueado/admin")
    public String cuentaAdminBloqueada() {
        return "admin/bloqueado";
    }

    @GetMapping("/bloqueado/operario")
    public String cuentaOperarioBloqueada() {
        return "operario/bloqueado";
    }

    @GetMapping("/bloqueado/caja")
    public String cuentaCajaBloqueada() {
        return "Caja/bloqueado";
    }

    @GetMapping("/bloqueado")
    public String cuentaBloqueada() {
        return "inicio";
    }
}
