package com.bike_assembly.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioController {

    // Esta será la nueva ruta principal de tu aplicación
    @GetMapping("/")
    public String mostrarInicio() {
        return "inicio"; // Buscará el archivo inicio.html
    }
}