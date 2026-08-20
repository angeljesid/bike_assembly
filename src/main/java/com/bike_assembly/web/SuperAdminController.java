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
        
        List<Usuario> listaAdmins = superAdminService.listarAdministradores();
        List<Usuario> listaClientes = superAdminService.listarClientes();
        long totalOrganizaciones = organizacionDao.count();

        model.addAttribute("nombreUsuario", username);
        model.addAttribute("listaAdmins", listaAdmins);
        model.addAttribute("listaClientes", listaClientes);
        model.addAttribute("totalOrganizaciones", totalOrganizaciones);
        model.addAttribute("totalAdmins", listaAdmins.size());
        
        return "superadmin/dashboard";
    }

    @PostMapping("/promover-admin")
    public String promoverAdmin(@RequestParam("idUsuario") Long idUsuario, 
                                @RequestParam("nombreTaller") String nombreTaller,
                                @RequestParam("direccionTaller") String direccionTaller,
                                @RequestParam("telefonoTaller") String telefonoTaller) {
        superAdminService.promoverAAdmin(idUsuario, nombreTaller, direccionTaller, telefonoTaller);
        return "redirect:/superadmin/dashboard?promovido=true";
    }
}