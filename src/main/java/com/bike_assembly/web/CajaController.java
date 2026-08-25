package com.bike_assembly.web;

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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/caja")
public class CajaController {

    @Autowired
    private ProductoDao productoDao;

    @Autowired
    private IVentaService ventaService;

    @Autowired
    private UsuarioDao usuarioDao;

    private Usuario obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);
    }

    @GetMapping({"", "/", "/pos"})
    public String vistaPos(Model model) {
        Usuario empleado = obtenerUsuarioAutenticado();
        model.addAttribute("empleado", empleado);
        return "caja/pos";
    }

    @GetMapping("/api/productos")
    @ResponseBody
    public ResponseEntity<List<Producto>> listarProductosCaja() {
        Usuario empleado = obtenerUsuarioAutenticado();
        if (empleado != null && empleado.getOrganizacion() != null) {
            List<Producto> productos = productoDao.findByOrganizacion(empleado.getOrganizacion());
            return ResponseEntity.ok(productos);
        }
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/api/buscar-producto")
    @ResponseBody
    public ResponseEntity<?> buscarProductoPorCodigo(@RequestParam("codigo") String codigoBarras) {
        Usuario empleado = obtenerUsuarioAutenticado();
        if (empleado != null && empleado.getOrganizacion() != null) {
            Optional<Producto> producto = productoDao.findByCodigoBarrasAndOrganizacion(codigoBarras, empleado.getOrganizacion());
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
        Usuario empleado = obtenerUsuarioAutenticado();
        
        model.addAttribute("venta", venta);
        model.addAttribute("empleado", empleado);
        return "caja/factura";
    }

    @GetMapping("/pedidos")
    public String listarPedidosPendientes(Model model) {
        Usuario empleado = obtenerUsuarioAutenticado();
        List<Venta> todosLosPedidos = ventaService.listarPedidosPendientes();

        if (empleado != null && empleado.getOrganizacion() != null) {
            todosLosPedidos = todosLosPedidos.stream().filter(v -> 
                v.getDetalles() != null && v.getDetalles().stream().anyMatch(d -> 
                    d.getProducto() != null && d.getProducto().getOrganizacion() != null &&
                    d.getProducto().getOrganizacion().getIdOrganizacion().equals(empleado.getOrganizacion().getIdOrganizacion())
                )
            ).collect(Collectors.toList());
        } else {
            todosLosPedidos = List.of();
        }

        List<Venta> pendientesFiltrados = todosLosPedidos.stream()
                .filter(v -> "PENDIENTE".equalsIgnoreCase(v.getEstado()))
                .collect(Collectors.toList());
                
        model.addAttribute("pedidos", pendientesFiltrados);
        model.addAttribute("empleado", empleado);
        return "caja/pedidos-pendientes";
    }

    @PostMapping("/pedidos/completar/{id}")
    public String completarPedido(@PathVariable("id") Long id) {
        ventaService.marcarPedidoCompletado(id);
        return "redirect:/caja/pedidos";
    }

    @PostMapping("/api/cliente/nuevo")
    @ResponseBody
    public ResponseEntity<?> registrarClienteRapido(@RequestBody Usuario nuevoCliente) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (nuevoCliente.getEmail() == null || nuevoCliente.getEmail().isEmpty()) {
                nuevoCliente.setEmail(nuevoCliente.getCedula() + "@consumidor.local");
            }
            
            nuevoCliente.setUsername(nuevoCliente.getCedula());
            nuevoCliente.setPassword("12345");
            nuevoCliente.setRol(Rol.CLIENTE);
            nuevoCliente.setActivo(true);
            
            Usuario guardado = usuarioDao.save(nuevoCliente);
            
            response.put("success", true);
            response.put("idUsuario", guardado.getIdUsuario());
            response.put("nombre", guardado.getNombre());
            response.put("cedula", guardado.getCedula());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("mensaje", "La cédula ya está registrada o hubo un error de base de datos.");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
}