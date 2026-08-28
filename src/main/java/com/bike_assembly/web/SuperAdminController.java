package com.bike_assembly.web;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.OrganizacionDao;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.services.usuario.ISuperAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

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
        
        List<Organizacion> organizaciones = organizacionDao.findAll();
        LocalDate hoy = LocalDate.now();
        
        for (Organizacion org : organizaciones) {
            if (org.getFechaVencimiento() != null && org.getFechaVencimiento().isBefore(hoy) && (org.getActivo() == null || org.getActivo())) {
                org.setActivo(false);
                organizacionDao.save(org);
                
                List<Usuario> vinculados = usuarioDao.findByOrganizacion(org);
                for(Usuario u : vinculados) {
                    u.setActivo(false);
                }
                usuarioDao.saveAll(vinculados);
            }
        }

        List<Usuario> listaAdmins = superAdminService.listarAdministradores();
        List<Usuario> listaClientes = superAdminService.listarClientes();
        List<Usuario> listaEmpleados = superAdminService.listarEmpleados(); 

        model.addAttribute("nombreUsuario", username);
        model.addAttribute("totalOrganizaciones", organizaciones.size());
        model.addAttribute("totalAdmins", listaAdmins.size());
        model.addAttribute("totalClientes", listaClientes.size());
        model.addAttribute("totalEmpleados", listaEmpleados.size());
        
        return "superadmin/dashboard";
    }

    @GetMapping("/usuarios")
    public String gestionarUsuarios(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        List<Usuario> listaAdmins = superAdminService.listarAdministradores();
        List<Usuario> listaClientes = superAdminService.listarClientes();
        List<Usuario> listaEmpleados = superAdminService.listarEmpleados();

        model.addAttribute("nombreUsuario", username);
        model.addAttribute("listaAdmins", listaAdmins);
        model.addAttribute("listaClientes", listaClientes);
        model.addAttribute("listaEmpleados", listaEmpleados);

        return "superadmin/usuarios";
    }

    @PostMapping("/promover-admin")
    public String promoverAdmin(@RequestParam("idUsuario") Long idUsuario, 
                                @RequestParam("nombreTaller") String nombreTaller,
                                @RequestParam("direccion") String direccionTaller,
                                @RequestParam("telefono") String telefonoTaller) {
        superAdminService.promoverAAdmin(idUsuario, nombreTaller, direccionTaller, telefonoTaller);
        return "redirect:/superadmin/usuarios?promovido=true";
    }

    @PostMapping("/actualizar-admin")
    public String actualizarAdmin(@RequestParam("idUsuario") Long idUsuario,
                                  @RequestParam("nombreTaller") String nombreTaller,
                                  @RequestParam("direccion") String direccion,
                                  @RequestParam("telefono") String telefono,
                                  @RequestParam("tipo") String tipo,
                                  @RequestParam(value = "fechaVencimiento", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaVencimiento) {
        superAdminService.actualizarAdminYOrganizacion(idUsuario, nombreTaller, direccion, telefono, tipo, fechaVencimiento);
        return "redirect:/superadmin/usuarios?actualizado=true";
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
        return "redirect:/superadmin/usuarios?estadoCambiado=true";
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
        return "redirect:/superadmin/usuarios?clienteActualizado=true";
    }

    @PostMapping("/estado-usuario")
    public String cambiarEstadoUsuario(@RequestParam("idUsuario") Long idUsuario, 
                                       @RequestParam("activo") Boolean activo) {
        Usuario usuario = usuarioDao.findById(idUsuario).orElse(null);
        
        if (usuario != null) {
            usuario.setActivo(activo);
            usuarioDao.save(usuario);
        }
        return "redirect:/superadmin/usuarios?estadoCambiado=true";
    }
}