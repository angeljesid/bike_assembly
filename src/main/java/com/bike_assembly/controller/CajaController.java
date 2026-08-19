package com.bike_assembly.controller;

import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.domain.Venta;
import com.bike_assembly.domain.VentaRequestDTO;
import com.bike_assembly.repository.ProductoDao;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.services.venta.IVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/caja")
public class CajaController {

    @Autowired
    private ProductoDao productoDao;

    @Autowired
    private IVentaService ventaService;

    @Autowired
    private UsuarioDao usuarioDao;

    @GetMapping({"", "/"})
    public String mostrarCaja() {
        return "admin/caja"; 
    }

    @GetMapping("/api/productos")
    @ResponseBody
    public ResponseEntity<List<Producto>> listarProductosCaja() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);
        
        if (admin != null && admin.getOrganizacion() != null) {
            List<Producto> productos = productoDao.findByOrganizacion(admin.getOrganizacion());
            return ResponseEntity.ok(productos);
        }
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/api/buscar-producto")
    @ResponseBody
    public ResponseEntity<?> buscarProductoPorCodigo(@RequestParam("codigo") String codigoBarras) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);
        
        if (admin != null && admin.getOrganizacion() != null) {
            Optional<Producto> producto = productoDao.findByCodigoBarrasAndOrganizacion(codigoBarras, admin.getOrganizacion());
            if (producto.isPresent()) {
                return ResponseEntity.ok(producto.get());
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/api/buscar-cliente")
    @ResponseBody
    public ResponseEntity<List<Usuario>> buscarCliente(@RequestParam("termino") String termino) {
        List<Usuario> clientes = usuarioDao.findByNombreContainingIgnoreCaseOrCedulaContainingIgnoreCaseAndRol(termino, termino, Rol.CLIENTE);
        return ResponseEntity.ok(clientes);
    }

    @PostMapping("/api/cobrar")
    @ResponseBody
    public ResponseEntity<?> cobrarVenta(@RequestBody VentaRequestDTO request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Venta ventaGuardada = ventaService.procesarCobro(request);
            response.put("success", true);
            response.put("idVenta", ventaGuardada.getIdVenta());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/factura/{id}")
    public String verFactura(@PathVariable("id") Long id, Model model) {
        Venta venta = ventaService.encontrarPorId(id);
        model.addAttribute("venta", venta);
        return "admin/factura";
    }
}