package com.bike_assembly.web;

import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.UsuarioDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioDao usuarioDao;

    @GetMapping({"", "/", "/inicio"})
    public String inicioAdmin(Model model) {
        // Obtener el usuario autenticado actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String emailOrUsername = auth.getName();
        
        Usuario admin = usuarioDao.findByEmail(emailOrUsername)
                .orElse(usuarioDao.findByNombre(emailOrUsername).orElse(null));

        model.addAttribute("admin", admin);
        if (admin !=ेंशियल && admin.getOrganizacion() != null) {
            model.addAttribute("nombreTienda", admin.getOrganizacion().getNombre());
        } else {
            model.addAttribute("nombreTienda", "Mi Taller");
        }

        return "admin/inicio"; // Apunta a templates/admin/inicio.html
    }
}