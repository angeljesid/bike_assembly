package com.bike_assembly.web;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.OrganizacionDao;
import com.bike_assembly.repository.UsuarioDao;
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
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/superadmin")
public class SuperAdminController {

    @Autowired
    private ISuperAdminService superAdminService;

    @Autowired
    private OrganizacionDao organizacionDao;

    @Autowired
    private UsuarioDao usuarioDao;

    @GetMapping("/dashboard")
    public String verDashboardGlobal(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        List<Usuario> listaAdmins = superAdminService.listarAdministradores();
        List<Usuario> listaClientes = superAdminService.listarClientes();
        long totalOrganizaciones = organizacionDao.count();

        Map<Long, List<Usuario>> clientesPorOrganizacion = listaClientes.stream()
            .filter(c -> c.getOrganizacion() != null)
            .collect(Collectors.groupingBy(c -> c.getOrganizacion().getIdOrganizacion()));

        model.addAttribute("nombreUsuario", username);
        model.addAttribute("listaAdmins", listaAdmins);
        model.addAttribute("listaClientes", listaClientes);
        model.addAttribute("clientesPorOrg", clientesPorOrganizacion);
        model.addAttribute("totalOrganizaciones", totalOrganizaciones);
        model.addAttribute("totalAdmins", listaAdmins.size());
        
        return "superadmin/dashboard";
    }

    @PostMapping("/promover-admin")
    public String promoverAdmin(@RequestParam("idUsuario") Long idUsuario, 
                                @RequestParam("nombreTaller") String nombreTaller,
                                @RequestParam("direccion") String direccionTaller,
                                @RequestParam("telefono") String telefonoTaller) {
        superAdminService.promoverAAdmin(idUsuario, nombreTaller, direccionTaller, telefonoTaller);
        return "redirect:/superadmin/dashboard?promovido=true";
    }

    @PostMapping("/actualizar-admin")
    public String actualizarAdmin(@RequestParam("idUsuario") Long idUsuario,
                                  @RequestParam("nombreTaller") String nombreTaller,
                                  @RequestParam("direccion") String direccion,
                                  @RequestParam("telefono") String telefono,
                                  @RequestParam("tipo") String tipo) {
        superAdminService.actualizarAdminYOrganizacion(idUsuario, nombreTaller, direccion, telefono, tipo);
        return "redirect:/superadmin/dashboard?actualizado=true";
    }

    @PostMapping("/estado-taller")
    public String cambiarEstadoTaller(@RequestParam("idOrganizacion") Long idOrganizacion, 
                                      @RequestParam("activo") Boolean activo) {
        Organizacion org = organizacionDao.findById(idOrganizacion).orElse(null);
        
        if (org != null) {
            org.setActivo(activo);
            organizacionDao.save(org);
            
            List<Usuario> vinculados = usuarioDao.findByOrganizacion(org);
            for(Usuario u : vinculados) {
                u.setActivo(activo);
            }
            usuarioDao.saveAll(vinculados);
        }
        return "redirect:/superadmin/dashboard?estadoCambiado=true";
    }

    @PostMapping("/actualizar-cliente")
    public String actualizarCliente(@RequestParam("idUsuario") Long idUsuario,
                                    @RequestParam("nombre") String nombre,
                                    @RequestParam("email") String email,
                                    @RequestParam("cedula") String cedula) {
        Usuario cliente = usuarioDao.findById(idUsuario).orElse(null);
        if (cliente != null) {
            cliente.setNombre(nombre);
            cliente.setEmail(email);
            cliente.setCedula(cedula);
            usuarioDao.save(cliente);
        }
        return "redirect:/superadmin/dashboard?clienteActualizado=true";
    }
}