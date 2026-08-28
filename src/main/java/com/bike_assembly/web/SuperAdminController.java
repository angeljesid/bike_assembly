package com.bike_assembly.web;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.services.usuario.ISuperAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/superadmin")
public class SuperAdminController {

    private static final int TAMANO_PAGINA = 10;

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
        List<Organizacion> vencidos = superAdminService.listarTalleresVencidos();
        int totalActivas = (int) organizaciones.stream().filter(o -> o.getActivo() == null || o.getActivo()).count();

        long totalAdmins = superAdminService.listarAdministradores(PageRequest.of(0, 1)).getTotalElements();
        long totalClientes = superAdminService.listarClientes(PageRequest.of(0, 1)).getTotalElements();
        long totalEmpleados = superAdminService.listarEmpleados(PageRequest.of(0, 1)).getTotalElements();

        model.addAttribute("nombreUsuario", username);
        model.addAttribute("totalOrganizaciones", organizaciones.size());
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("totalEmpleados", totalEmpleados);
        model.addAttribute("talleresPorVencer", porVencer);
        model.addAttribute("talleresSuspendidos", suspendidas);
        model.addAttribute("talleresVencidos", vencidos);
        model.addAttribute("mapaTipos", superAdminService.contarTalleresPorTipo());
        model.addAttribute("totalActivas", totalActivas);
        model.addAttribute("ingresosActivos", superAdminService.obtenerIngresosActivos());

        return "superadmin/dashboard";
    }

    @GetMapping("/usuarios")
    public String gestionarUsuarios(Model model,
                                    @RequestParam(value = "paginaAdmins", defaultValue = "0") int paginaAdmins,
                                    @RequestParam(value = "paginaClientes", defaultValue = "0") int paginaClientes,
                                    @RequestParam(value = "paginaEmpleados", defaultValue = "0") int paginaEmpleados) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Pageable pageAdmins = PageRequest.of(Math.max(0, paginaAdmins), TAMANO_PAGINA, Sort.by("idUsuario"));
        Pageable pageClientes = PageRequest.of(Math.max(0, paginaClientes), TAMANO_PAGINA, Sort.by("idUsuario"));
        Pageable pageEmpleados = PageRequest.of(Math.max(0, paginaEmpleados), TAMANO_PAGINA, Sort.by("idUsuario"));

        Page<Usuario> admins = superAdminService.listarAdministradores(pageAdmins);
        Page<Usuario> clientes = superAdminService.listarClientes(pageClientes);
        Page<Usuario> empleados = superAdminService.listarEmpleados(pageEmpleados);

        model.addAttribute("nombreUsuario", username);
        model.addAttribute("admins", admins);
        model.addAttribute("clientes", clientes);
        model.addAttribute("empleados", empleados);
        model.addAttribute("tituloTamano", TAMANO_PAGINA);
        model.addAttribute("administradoresHuerfanos", superAdminService.listarAdministradoresHuerfanos());

        return "superadmin/usuarios";
    }

    @PostMapping("/promover-admin")
    public String promoverAdmin(@RequestParam("idUsuario") Long idUsuario, 
                                @RequestParam("nombreTaller") String nombreTaller,
                                @RequestParam("direccion") String direccionTaller,
                                @RequestParam("telefono") String telefonoTaller,
                                @RequestParam("tipo") String tipo,
                                @RequestParam(value = "precioPlan", required = false) BigDecimal precioPlan,
                                @RequestParam(value = "fechaVencimiento", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaVencimiento) {
        try {
            superAdminService.promoverAAdmin(idUsuario, nombreTaller, direccionTaller, telefonoTaller, tipo, precioPlan, fechaVencimiento);
        } catch (Exception e) {
            return "redirect:/superadmin/usuarios?promovido=false";
        }
        return "redirect:/superadmin/usuarios?promovido=true";
    }

    @PostMapping("/actualizar-admin")
    public String actualizarAdmin(@RequestParam("idUsuario") Long idUsuario,
                                  @RequestParam("nombreTaller") String nombreTaller,
                                  @RequestParam("direccion") String direccion,
                                  @RequestParam("telefono") String telefono,
                                  @RequestParam("tipo") String tipo,
                                  @RequestParam(value = "precioPlan", required = false) BigDecimal precioPlan,
                                  @RequestParam(value = "fechaVencimiento", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaVencimiento) {
        try {
            superAdminService.actualizarAdminYOrganizacion(idUsuario, nombreTaller, direccion, telefono, tipo, precioPlan, fechaVencimiento);
        } catch (Exception e) {
            return "redirect:/superadmin/usuarios?actualizado=false";
        }
        return "redirect:/superadmin/usuarios?actualizado=true";
    }

    @PostMapping("/estado-taller")
    public String cambiarEstadoTaller(@RequestParam("idOrganizacion") Long idOrganizacion, 
                                      @RequestParam("activo") Boolean activo,
                                      @RequestParam(value = "motivo", required = false) String motivo) {
        if (Boolean.FALSE.equals(activo)) {
            try {
                superAdminService.cambiarEstadoTaller(idOrganizacion, activo, motivo);
            } catch (Exception e) {
                return "redirect:/superadmin/usuarios?estadoCambiado=false";
            }
            return "redirect:/superadmin/usuarios?estadoCambiado=true";
        }
        try {
            superAdminService.cambiarEstadoTaller(idOrganizacion, activo, null);
        } catch (Exception e) {
            return "redirect:/superadmin/usuarios?estadoCambiado=false";
        }
        return "redirect:/superadmin/usuarios?estadoCambiado=true";
    }

    @PostMapping("/asignar-taller")
    public String asignarTaller(@RequestParam("idUsuario") Long idUsuario,
                                @RequestParam("nombreTaller") String nombreTaller,
                                @RequestParam("direccion") String direccion,
                                @RequestParam("telefono") String telefono,
                                @RequestParam("tipo") String tipo,
                                @RequestParam(value = "precioPlan", required = false) BigDecimal precioPlan,
                                @RequestParam(value = "fechaVencimiento", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaVencimiento) {
        try {
            superAdminService.asignarTallerYPlan(idUsuario, nombreTaller, direccion, telefono, tipo, precioPlan, fechaVencimiento);
        } catch (Exception e) {
            return "redirect:/superadmin/usuarios?asignado=false";
        }
        return "redirect:/superadmin/usuarios?asignado=true";
    }

    @PostMapping("/actualizar-cliente")
    public String actualizarCliente(@RequestParam("idUsuario") Long idUsuario,
                                    @RequestParam("nombre") String nombre,
                                    @RequestParam("email") String email,
                                    @RequestParam("cedula") String cedula) {
        try {
            superAdminService.actualizarDatosBasicosUsuario(idUsuario, nombre, cedula, email);
        } catch (Exception e) {
            return "redirect:/superadmin/usuarios?clienteActualizado=false";
        }
        return "redirect:/superadmin/usuarios?clienteActualizado=true";
    }

    @PostMapping("/actualizar-empleado")
    public String actualizarEmpleado(@RequestParam("idUsuario") Long idUsuario,
                                     @RequestParam("nombre") String nombre,
                                     @RequestParam("email") String email,
                                     @RequestParam("cedula") String cedula) {
        try {
            superAdminService.actualizarEmpleado(idUsuario, nombre, cedula, email);
        } catch (Exception e) {
            return "redirect:/superadmin/usuarios?empleadoActualizado=false";
        }
        return "redirect:/superadmin/usuarios?empleadoActualizado=true";
    }

    @PostMapping("/estado-usuario")
    public String cambiarEstadoUsuario(@RequestParam("idUsuario") Long idUsuario, 
                                       @RequestParam("activo") Boolean activo) {
        try {
            superAdminService.cambiarEstadoUsuario(idUsuario, activo);
        } catch (Exception e) {
            return "redirect:/superadmin/usuarios?estadoCambiado=false";
        }
        return "redirect:/superadmin/usuarios?estadoCambiado=true";
    }
}
