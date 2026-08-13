package com.bike_assembly.web;

import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.OrganizacionDao;
import com.bike_assembly.services.usuario.ISuperAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
        
        // 1. Obtener el nombre del usuario que inició sesión (Tú)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // 2. Obtener los datos reales de la base de datos
        List<Usuario> listaAdmins = superAdminService.listarAdministradores();
        long totalOrganizaciones = organizacionDao.count();
        int totalAdmins = listaAdmins.size();

        // 3. Enviar los datos al HTML (Thymeleaf)
        model.addAttribute("nombreUsuario", username);
        model.addAttribute("listaAdmins", listaAdmins);
        model.addAttribute("totalOrganizaciones", totalOrganizaciones);
        model.addAttribute("totalAdmins", totalAdmins);

        // 4. Retornar la vista que creamos en el Paso 1
        return "superadmin/dashboard";
    }
}