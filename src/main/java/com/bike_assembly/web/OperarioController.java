package com.bike_assembly.web;

import com.bike_assembly.domain.Usuario;
import com.bike_assembly.domain.Venta;
import com.bike_assembly.domain.EstadoServicio;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.repository.VentaDao;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/operario")
public class OperarioController {

    @Autowired
    private UsuarioDao usuarioDao;
    
    @Autowired
    private VentaDao ventaDao;

    private Usuario obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);
    }

    private List<Venta> filtrarPorOrganizacion(List<Venta> ventas, Usuario operario) {
        if (operario == null || operario.getOrganizacion() == null) {
            return List.of();
        }
        Long orgId = operario.getOrganizacion().getIdOrganizacion();
        
        return ventas.stream().filter(v -> {
            if (v.getEmpleado() != null && v.getEmpleado().getOrganizacion() != null) {
                if (v.getEmpleado().getOrganizacion().getIdOrganizacion().equals(orgId)) {
                    return true;
                }
            }
            if (v.getDetalles() != null) {
                return v.getDetalles().stream().anyMatch(d -> 
                    d.getProducto() != null && 
                    d.getProducto().getOrganizacion() != null &&
                    d.getProducto().getOrganizacion().getIdOrganizacion().equals(orgId)
                );
            }
            return false;
        }).collect(Collectors.toList());
    }

    @GetMapping({"", "/", "/ensambles"})
    public String inicioOperario(Model model) {
        Usuario operario = obtenerUsuarioAutenticado();
        model.addAttribute("operario", operario);

        return "operario/ensambles";
    }

    @GetMapping("/kanban")
    public String tableroKanban(Model model) {
        Usuario operario = obtenerUsuarioAutenticado();
        model.addAttribute("operario", operario);
        
        model.addAttribute("pendientes", filtrarPorOrganizacion(ventaDao.findByEstado(EstadoServicio.PENDIENTE.name()), operario));
        model.addAttribute("enDiagnostico", filtrarPorOrganizacion(ventaDao.findByEstado(EstadoServicio.EN_DIAGNOSTICO.name()), operario));
        model.addAttribute("enReparacion", filtrarPorOrganizacion(ventaDao.findByEstado(EstadoServicio.EN_REPARACION.name()), operario));
        model.addAttribute("esperandoRepuesto", filtrarPorOrganizacion(ventaDao.findByEstado(EstadoServicio.ESPERANDO_REPUESTO.name()), operario));
        model.addAttribute("terminados", filtrarPorOrganizacion(ventaDao.findByEstado(EstadoServicio.TERMINADO.name()), operario));

        return "operario/kanban";
    }

    @PostMapping("/actualizar-estado")
    public String actualizarEstado(@RequestParam Long ventaId, @RequestParam String nuevoEstado) {
        Usuario operario = obtenerUsuarioAutenticado();
        Venta venta = ventaDao.findById(ventaId).orElse(null);
        
        if (venta != null && operario != null && operario.getOrganizacion() != null) {
            Long orgId = operario.getOrganizacion().getIdOrganizacion();
            boolean pertenece = false;
            
            if (venta.getEmpleado() != null && venta.getEmpleado().getOrganizacion() != null) {
                pertenece = venta.getEmpleado().getOrganizacion().getIdOrganizacion().equals(orgId);
            }
            if (!pertenece && venta.getDetalles() != null) {
                pertenece = venta.getDetalles().stream().anyMatch(d -> 
                    d.getProducto() != null && 
                    d.getProducto().getOrganizacion() != null &&
                    d.getProducto().getOrganizacion().getIdOrganizacion().equals(orgId)
                );
            }
            
            if (pertenece) {
                venta.setEstado(nuevoEstado);
                ventaDao.save(venta);
            }
        }
        return "redirect:/operario/kanban";
    }
}