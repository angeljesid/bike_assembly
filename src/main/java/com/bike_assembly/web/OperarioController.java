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

@Controller
@RequestMapping("/operario")
public class OperarioController {

    @Autowired
    private UsuarioDao usuarioDao;
    
    @Autowired
    private VentaDao ventaDao;

    @GetMapping({"", "/", "/ensambles"})
    public String inicioOperario(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usernameOrEmail = auth.getName();
        
        Usuario operario = usuarioDao.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElse(null);
        model.addAttribute("operario", operario);

        return "operario/ensambles";
    }

    @GetMapping("/kanban")
    public String tableroKanban(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usernameOrEmail = auth.getName();
        
        Usuario operario = usuarioDao.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElse(null);
        model.addAttribute("operario", operario);
        
        model.addAttribute("pendientes", ventaDao.findByEstado(EstadoServicio.PENDIENTE.name()));
        model.addAttribute("enDiagnostico", ventaDao.findByEstado(EstadoServicio.EN_DIAGNOSTICO.name()));
        model.addAttribute("enReparacion", ventaDao.findByEstado(EstadoServicio.EN_REPARACION.name()));
        model.addAttribute("esperandoRepuesto", ventaDao.findByEstado(EstadoServicio.ESPERANDO_REPUESTO.name()));
        model.addAttribute("terminados", ventaDao.findByEstado(EstadoServicio.TERMINADO.name()));

        return "operario/kanban";
    }

    @PostMapping("/actualizar-estado")
    public String actualizarEstado(@RequestParam Long ventaId, @RequestParam String nuevoEstado) {
        Venta venta = ventaDao.findById(ventaId).orElse(null);
        if (venta != null) {
            venta.setEstado(nuevoEstado);
            ventaDao.save(venta);
        }
        return "redirect:/operario/kanban";
    }
}