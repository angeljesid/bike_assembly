package com.bike_assembly.controller;

import com.bike_assembly.domain.Producto;
import com.bike_assembly.services.producto.IProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
@RequestMapping("/catalogo")


public class homeController {

    @Autowired
    private IProductoService productoService;

    @GetMapping("/login")
    public String home() {
        // Retorna la vista ubicada en templates/admin/login.html
        return "admin/login"; 
    }

    // Ruta principal que renderiza la vista 'producto.html'
    @GetMapping
    public String inicio(@RequestParam(name = "tipo", required = false) String tipo, Model model) {
        if (tipo != null && !tipo.isEmpty()) {
            // Filtra los componentes compatibles según el tipo de bicicleta seleccionado (T, S) + Universales (U)
            List<Producto> componentes = productoService.obtenerCompatibles(tipo.toUpperCase());
            model.addAttribute("componentes", componentes);
            model.addAttribute("tipoSeleccionado", tipo.toUpperCase());
        } else {
            // Si entra por primera vez sin parámetros, lista todo el inventario disponible
            model.addAttribute("componentes", productoService.listarProductos());
        }
        
        // Retorna la vista ubicada en templates/admin/producto.html
        return "admin/producto"; 
    }
}