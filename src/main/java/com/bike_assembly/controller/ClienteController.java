package com.bike_assembly.controller;

import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.UsuarioDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private UsuarioDao usuarioDao;

    @GetMapping({"", "/", "/catalogo"})
    public String catalogoCliente(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usernameOrEmail = auth.getName();
        
        Usuario cliente = usuarioDao.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElse(null);
        model.addAttribute("cliente", cliente);

        return "cliente/catalogo"; // Apunta a templates/cliente/catalogo.html
    }

    @GetMapping("/producto/{id}")
    public String detalleProducto(@PathVariable("id") Long id, Model model) {
        // En un futuro cercano aquí buscaremos el producto en la BD para mandarlo a la vista
        model.addAttribute("idProducto", id);
        return "cliente/producto-detalle"; 
    }

    @GetMapping("/carrito")
    public String verCarrito(Model model) {
        return "cliente/carrito"; 
    }
}