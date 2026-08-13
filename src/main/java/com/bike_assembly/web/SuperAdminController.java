package com.bike_assembly.web;

import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.OrganizacionDao;
import com.bike_assembly.services.SuperAdmin.ISuperAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/superadmin")
public class SuperAdminController {

    @Autowired
    private ISuperAdminService superAdminService;

    @Autowired
    private OrganizacionDao organizacionDao;

   @GetMapping("/dashboard")
    public String verDashboardGlobal(Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        List<Usuario> listaUsuarios = superAdminService.listarTodosLosUsuarios();
        
        long totalOrganizaciones = organizacionDao.count();

        model.addAttribute("nombreUsuario", username);
        model.addAttribute("listaUsuarios", listaUsuarios);
        model.addAttribute("totalOrganizaciones", totalOrganizaciones);
        model.addAttribute("totalUsuarios", listaUsuarios.size());

        return "superadmin/dashboard";
    }

    @PostMapping("/cambiar-rol")
    public String procesarCambioRol(@RequestParam("idUsuario") Long idUsuario, 
                                    @RequestParam("nuevoRol") Rol nuevoRol) {
        
        superAdminService.cambiarRolUsuario(idUsuario, nuevoRol);
        
        return "redirect:/superadmin/dashboard?actualizado=true";
    }
}