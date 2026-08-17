package com.bike_assembly.controller;

import com.bike_assembly.domain.Usuario;
import com.bike_assembly.domain.Venta;
import com.bike_assembly.domain.VentaRequestDTO;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.services.venta.IVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private IVentaService ventaService;

    @GetMapping({"", "/", "/catalogo"})
    public String catalogoCliente(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usernameOrEmail = auth.getName();
        
        Usuario cliente = usuarioDao.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElse(null);
        model.addAttribute("cliente", cliente);

        return "cliente/catalogo"; 
    }

    @GetMapping("/producto/{id}")
    public String detalleProducto(@PathVariable("id") Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usernameOrEmail = auth.getName();
        Usuario cliente = usuarioDao.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElse(null);
        
        model.addAttribute("cliente", cliente);
        model.addAttribute("idProducto", id);
        return "cliente/producto-detalle"; 
    }

    @GetMapping("/carrito")
    public String verCarrito(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usernameOrEmail = auth.getName();
        
        Usuario cliente = usuarioDao.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElse(null);
        model.addAttribute("cliente", cliente);

        return "cliente/carrito"; 
    }

    @GetMapping("/pedidos")
    public String misPedidos(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usernameOrEmail = auth.getName();
        
        Usuario cliente = usuarioDao.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElse(null);
        
        if (cliente != null) {
            List<Venta> todasLasVentas = ventaService.listarPedidosCliente(cliente);
            
            List<Venta> pendientes = todasLasVentas.stream()
                    .filter(v -> "PENDIENTE".equals(v.getEstado()))
                    .collect(Collectors.toList());
                    
            List<Venta> completadas = todasLasVentas.stream()
                    .filter(v -> "COMPLETADA".equals(v.getEstado()))
                    .collect(Collectors.toList());
            
            model.addAttribute("pendientes", pendientes);
            model.addAttribute("completadas", completadas);
            model.addAttribute("cliente", cliente);
        }
        
        return "cliente/mis-pedidos"; 
    }

    @PostMapping("/api/checkout")
    @ResponseBody
    public ResponseEntity<?> procesarCheckout(@RequestBody VentaRequestDTO request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String usernameOrEmail = auth.getName();
            Usuario cliente = usuarioDao.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

            ventaService.procesarPedidoCliente(request, cliente);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}