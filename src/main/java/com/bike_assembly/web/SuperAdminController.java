package com.bike_assembly.web;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Usuario;
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

    @GetMapping("/dashboard")
    public String verDashboardGlobal(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        superAdminService.sincronizarTalleresVencidos();

        List<Organizacion> organizaciones = superAdminService.listarOrganizaciones();
        List<Organizacion> porVencer = superAdminService.listarOrganizacionesPorVencer(30);
        List<Organizacion> suspendidas = superAdminService.listarOrganizacionesSuspendidas();

        List<Usuario> listaAdmins = superAdminService.listarAdministradores();
        List<Usuario> listaClientes = superAdminService.listarClientes();
        List<Usuario> listaEmpleados = superAdminService.listarEmpleados(); 

        model.addAttribute("nombreUsuario", username);
        model.addAttribute("totalOrganizaciones", organizaciones.size());
        model.addAttribute("totalAdmins", listaAdmins.size());
        model.addAttribute("totalClientes", listaClientes.size());
        model.addAttribute("totalEmpleados", listaEmpleados.size());
        model.addAttribute("talleresPorVencer", porVencer);
        model.addAttribute("talleresSuspendidos", suspendidas);

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
                                @RequestParam("telefono") String telefonoTaller,
                                @RequestParam("tipo") String tipo,
                                @RequestParam(value = "fechaVencimiento", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaVencimiento) {
        superAdminService.promoverAAdmin(idUsuario, nombreTaller, direccionTaller, telefonoTaller, tipo, fechaVencimiento);
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
        superAdminService.cambiarEstadoTaller(idOrganizacion, activo);
        return "redirect:/superadmin/usuarios?estadoCambiado=true";
    }

    @PostMapping("/actualizar-cliente")
    public String actualizarCliente(@RequestParam("idUsuario") Long idUsuario,
                                    @RequestParam("nombre") String nombre,
                                    @RequestParam("email") String email,
                                    @RequestParam("cedula") String cedula) {
        superAdminService.actualizarDatosBasicosUsuario(idUsuario, nombre, cedula, email);
        return "redirect:/superadmin/usuarios?clienteActualizado=true";
    }

    @PostMapping("/actualizar-empleado")
    public String actualizarEmpleado(@RequestParam("idUsuario") Long idUsuario,
                                     @RequestParam("nombre") String nombre,
                                     @RequestParam("email") String email,
                                     @RequestParam("cedula") String cedula) {
        superAdminService.actualizarEmpleado(idUsuario, nombre, cedula, email);
        return "redirect:/superadmin/usuarios?empleadoActualizado=true";
    }

    @PostMapping("/estado-usuario")
    public String cambiarEstadoUsuario(@RequestParam("idUsuario") Long idUsuario, 
                                       @RequestParam("activo") Boolean activo) {
        superAdminService.cambiarEstadoUsuario(idUsuario, activo);
        return "redirect:/superadmin/usuarios?estadoCambiado=true";
    }
}
