package com.bike_assembly.web;

import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.security.SessionUtils;
import com.bike_assembly.services.ordenservicio.IOrdenServicioService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private IOrdenServicioService ordenServicioService;

    @Autowired
    private SessionUtils sessionUtils;

    private Usuario obtenerUsuarioAutenticado() {
        return sessionUtils.usuarioActual();
    }

    @GetMapping({"", "/", "/ensambles"})
    public String inicioOperario(Model model) {
        Usuario operario = obtenerUsuarioAutenticado();
        model.addAttribute("operario", operario);

        return "operario/ensambles";
    }

    @GetMapping("/cola")
    public String colaPedidosServicio(Model model) {
        Usuario operario = obtenerUsuarioAutenticado();
        model.addAttribute("operario", operario);
        if (operario != null) {
            model.addAttribute("enCola", ordenServicioService.listarEnCola(operario.getOrganizacion()));
        } else {
            model.addAttribute("enCola", List.of());
        }
        return "operario/cola-servicio";
    }

    @PostMapping("/tomar")
    public String tomarOrden(@RequestParam Long ordenId) {
        Usuario operario = obtenerUsuarioAutenticado();
        if (operario != null) {
            ordenServicioService.tomarOrden(ordenId, operario);
        }
        return "redirect:/operario/cola";
    }

    @GetMapping("/kanban-servicios")
    public String kanbanServicios(Model model) {
        Usuario operario = obtenerUsuarioAutenticado();
        model.addAttribute("operario", operario);
        if (operario != null) {
            List<com.bike_assembly.domain.OrdenServicio> mis = ordenServicioService.listarPorOperario(operario);
            model.addAttribute("enDiagnostico", filtrarOrdenes(mis, com.bike_assembly.domain.EstadoOrdenServicio.EN_DIAGNOSTICO.name()));
            model.addAttribute("enReparacion", filtrarOrdenes(mis, com.bike_assembly.domain.EstadoOrdenServicio.EN_REPARACION.name()));
            model.addAttribute("esperandoRepuesto", filtrarOrdenes(mis, com.bike_assembly.domain.EstadoOrdenServicio.ESPERANDO_REPUESTO.name()));
            model.addAttribute("terminados", filtrarOrdenes(mis, com.bike_assembly.domain.EstadoOrdenServicio.TERMINADO.name()));
            model.addAttribute("totalTomadas", mis.size());
            model.addAttribute("enProceso", mis.stream()
                    .filter(o -> !com.bike_assembly.domain.EstadoOrdenServicio.TERMINADO.name().equals(o.getEstado()))
                    .count());
        } else {
            model.addAttribute("enDiagnostico", List.of());
            model.addAttribute("enReparacion", List.of());
            model.addAttribute("esperandoRepuesto", List.of());
            model.addAttribute("terminados", List.of());
            model.addAttribute("totalTomadas", 0);
            model.addAttribute("enProceso", 0);
        }
        return "operario/kanban-servicio";
    }

    @PostMapping("/actualizar-estado-servicio")
    public String actualizarEstadoServicio(@RequestParam Long ordenId, @RequestParam String nuevoEstado) {
        Usuario operario = obtenerUsuarioAutenticado();
        if (operario != null) {
            ordenServicioService.actualizarEstado(ordenId, nuevoEstado, operario);
        }
        return "redirect:/operario/kanban-servicios";
    }

    private java.util.List<com.bike_assembly.domain.OrdenServicio> filtrarOrdenes(
            java.util.List<com.bike_assembly.domain.OrdenServicio> ordenes, String estado) {
        return ordenes.stream()
                .filter(o -> estado.equals(o.getEstado()))
                .collect(Collectors.toList());
    }
}