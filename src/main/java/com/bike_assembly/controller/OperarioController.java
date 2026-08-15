package com.bike_assembly.controller;

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
@RequestMapping("/operario")
public class OperarioController {

    @Autowired
    private UsuarioDao usuarioDao;

    @GetMapping({"", "/", "/ensambles"})
    public String inicioOperario(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usernameOrEmail = auth.getName();
        
        Usuario operario = usuarioDao.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElse(null);
        model.addAttribute("operario", operario);

        return "operario/ensambles";
    }
}