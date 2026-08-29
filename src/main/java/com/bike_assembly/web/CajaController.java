package com.bike_assembly.web;

import com.bike_assembly.domain.OrdenServicio;
import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.RepuestoOrdenServicio;
import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.domain.Venta;
import com.bike_assembly.domain.VentaRequestDTO;
import com.bike_assembly.repository.ProductoDao;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.security.SessionUtils;
import com.bike_assembly.services.ordenservicio.IOrdenServicioService;
import com.bike_assembly.services.solicitud.ISolicitudRepuestoService;
import com.bike_assembly.services.venta.IVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    private IOrdenServicioService ordenServicioService;

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private ISolicitudRepuestoService solicitudRepuestoService;

    @Autowired
    private SessionUtils sessionUtils;

    private Usuario obtenerUsuarioAutenticado() {
        return sessionUtils.usuarioActual();
    }

    @GetMapping({"", "/", "/caja", "/pos"})
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
            List<Producto> productos = productoDao.findByOrganizacionAndEliminadoFalse(empleado.getOrganizacion());
            return ResponseEntity.ok(productos);
        }
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/api/buscar-producto")
    @ResponseBody
    public ResponseEntity<?> buscarProductoPorCodigo(@RequestParam("codigo") String codigoBarras) {
        Usuario empleado = obtenerUsuarioAutenticado();
        if (empleado != null && empleado.getOrganizacion() != null) {
            Optional<Producto> producto = productoDao.findByCodigoBarrasAndOrganizacionAndEliminadoFalse(codigoBarras, empleado.getOrganizacion());
            if (producto.isPresent()) {
                return ResponseEntity.ok(producto.get());
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/api/buscar-cliente")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> buscarCliente(@RequestParam("termino") String termino) {
        List<Usuario> clientes = usuarioDao.findByNombreContainingIgnoreCaseOrCedulaContainingIgnoreCaseAndRol(termino, termino, Rol.CLIENTE);
        List<Map<String, Object>> resultado = clientes.stream().map(c -> {
            Map<String, Object> datos = new HashMap<>();
            datos.put("idUsuario", c.getIdUsuario());
            datos.put("nombre", c.getNombre());
            datos.put("cedula", c.getCedula());
            return datos;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/api/cobrar")
    @ResponseBody
    public ResponseEntity<?> cobrarVenta(@RequestBody VentaRequestDTO request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Venta ventaGuardada = ventaService.procesarCobro(request, obtenerUsuarioAutenticado());
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
        
        if (venta != null && empleado != null && empleado.getOrganizacion() != null) {
            Long orgId = empleado.getOrganizacion().getIdOrganizacion();
            if (!ventaService.ventaPerteneceAOrganizacion(venta, orgId)) {
                return "redirect:/caja";
            }
        }
        
        model.addAttribute("venta", venta);
        model.addAttribute("empleado", empleado);
        return "caja/factura";
    }

    @GetMapping("/pedidos")
    public String listarPedidosPendientes(Model model) {
        Usuario empleado = obtenerUsuarioAutenticado();
        List<Venta> pendientesFiltrados = empleado != null && empleado.getOrganizacion() != null
                ? ventaService.listarVentasPorOrganizacion(empleado.getOrganizacion().getIdOrganizacion(), "PENDIENTE")
                : List.of();
        List<com.bike_assembly.domain.SolicitudRepuesto> solicitudes =
                empleado != null ? solicitudRepuestoService.listarPendientes(empleado.getOrganizacion()) : List.of();
                
        model.addAttribute("pedidos", pendientesFiltrados);
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("empleado", empleado);
        return "caja/pedidos-pendientes";
    }

    @PostMapping("/pedidos/aprobar-insumo/{id}")
    public String aprobarInsumo(@PathVariable("id") Long id) {
        solicitudRepuestoService.aceptar(id, obtenerUsuarioAutenticado());
        return "redirect:/caja/pedidos";
    }

    @PostMapping("/pedidos/rechazar-insumo/{id}")
    public String rechazarInsumo(@PathVariable("id") Long id) {
        solicitudRepuestoService.rechazar(id, obtenerUsuarioAutenticado());
        return "redirect:/caja/pedidos";
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

    @GetMapping("/servicio/nuevo")
    public String registrarServicioForm(Model model) {
        Usuario empleado = obtenerUsuarioAutenticado();
        model.addAttribute("empleado", empleado);
        model.addAttribute("orden", new OrdenServicio());
        return "caja/registrar-servicio";
    }

    @PostMapping("/servicio/guardar")
    public String guardarServicio(OrdenServicio orden,
                                  @RequestParam(value = "clienteId", required = false) Long clienteId,
                                  @RequestParam(value = "precioServicioVal", required = false) Double precioServicioVal,
                                  @RequestParam(value = "repuestoProductoId", required = false) List<Long> repuestoProductoId,
                                  @RequestParam(value = "repuestoCantidad", required = false) List<Integer> repuestoCantidad,
                                  @RequestParam(value = "repuestoPrecio", required = false) List<Double> repuestoPrecio) {
        Usuario empleado = obtenerUsuarioAutenticado();
        if (empleado != null && empleado.getOrganizacion() != null) {
            if (clienteId != null) {
                Optional<Usuario> clienteOpt = usuarioDao.findById(clienteId);
                if (clienteOpt.isPresent() && clienteOpt.get().getRol() == Rol.CLIENTE) {
                    Usuario cliente = clienteOpt.get();
                    orden.setClienteUsuario(cliente);
                    orden.setClienteNombre(cliente.getNombre());
                    orden.setClienteCedula(cliente.getCedula());
                }
            }
            orden.setPrecioServicio(precioServicioVal != null ? precioServicioVal : 0.0);

            List<RepuestoOrdenServicio> repuestos = new ArrayList<>();
            if (repuestoProductoId != null && repuestoCantidad != null && repuestoPrecio != null) {
                for (int i = 0; i < repuestoProductoId.size(); i++) {
                    Long prodId = repuestoProductoId.get(i);
                    Integer cant = i < repuestoCantidad.size() ? repuestoCantidad.get(i) : 1;
                    Double precio = i < repuestoPrecio.size() ? repuestoPrecio.get(i) : 0.0;
                    if (prodId == null) {
                        continue;
                    }
                    Optional<Producto> prodOpt = productoDao.findById(prodId);
                    if (prodOpt.isEmpty()) {
                        continue;
                    }
                    RepuestoOrdenServicio rep = new RepuestoOrdenServicio();
                    rep.setProducto(prodOpt.get());
                    rep.setCantidad(cant != null && cant > 0 ? cant : 1);
                    rep.setPrecioUnitario(precio != null ? precio : 0.0);
                    rep.setSubtotal(rep.getPrecioUnitario() * rep.getCantidad());
                    repuestos.add(rep);
                }
            }
            orden.setRepuestos(repuestos);

            ordenServicioService.registrar(orden, empleado);
            return "redirect:/caja/servicio/nuevo?exito=true";
        }
        return "redirect:/caja/servicio/nuevo";
    }
}