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

    @GetMapping({"", "/", "/inicio", "/dashboard"})
    public String inicioAdmin(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usernameOrEmail = auth.getName();
        
        // Buscamos de forma segura por email
        Usuario admin = usuarioDao.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElse(null);

        model.addAttribute("admin", admin);
        
        // Verificación limpia del if sin caracteres raros
        if (admin != null && admin.getOrganizacion() != null) {
            model.addAttribute("nombreTienda", admin.getOrganizacion().getNombre());
        } else {
            model.addAttribute("nombreTienda", "Mi Taller");
        }

        return "admin/inicio"; 
    }
}