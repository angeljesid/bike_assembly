package com.bike_assembly.web;

import com.bike_assembly.domain.Usuario;
import com.bike_assembly.domain.Venta;
import com.bike_assembly.domain.OrdenServicio;
import com.bike_assembly.domain.VentaRequestDTO;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.security.SessionUtils;
import com.bike_assembly.services.ordenservicio.IOrdenServicioService;
import com.bike_assembly.services.venta.IVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private IVentaService ventaService;

    @Autowired
    private IOrdenServicioService ordenServicioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario obtenerClienteAutenticado() {
        return sessionUtils.usuarioActual();
    }

    @GetMapping({"", "/", "/catalogo"})
    public String catalogoCliente(Model model) {
        Usuario cliente = obtenerClienteAutenticado();
        model.addAttribute("cliente", cliente);

        return "cliente/catalogo"; 
    }

    @GetMapping("/producto/{id}")
    public String detalleProducto(@PathVariable("id") Long id, Model model) {
        Usuario cliente = obtenerClienteAutenticado();
        
        model.addAttribute("cliente", cliente);
        model.addAttribute("idProducto", id);
        return "cliente/producto-detalle"; 
    }

    @GetMapping({"/carrito", "/guardados"})
    public String verCarrito(Model model) {
        Usuario cliente = obtenerClienteAutenticado();
        model.addAttribute("cliente", cliente);

        return "cliente/carrito"; 
    }

    @GetMapping("/pedidos")
    public String misPedidos(Model model) {
        Usuario cliente = obtenerClienteAutenticado();
        
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

    @GetMapping("/servicios")
    public String misServicios(Model model) {
        Usuario cliente = obtenerClienteAutenticado();

        if (cliente != null) {
            List<OrdenServicio> ordenes = ordenServicioService.listarPorCliente(cliente);
            model.addAttribute("ordenes", ordenes);
            model.addAttribute("enProceso", ordenes.stream()
                    .filter(o -> !"TERMINADO".equals(o.getEstado()))
                    .count());
            model.addAttribute("terminados", ordenes.stream()
                    .filter(o -> "TERMINADO".equals(o.getEstado()))
                    .count());
            model.addAttribute("cliente", cliente);
        }

        return "cliente/seguimiento-servicios";
    }

    @GetMapping("/perfil")
    public String verPerfil(@RequestParam(value = "guardado", required = false) String guardado,
                            Model model) {
        Usuario cliente = obtenerClienteAutenticado();
        if (cliente == null) {
            return "redirect:/login";
        }
        model.addAttribute("cliente", cliente);
        model.addAttribute("guardado", "true".equals(guardado));
        return "cliente/perfil";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(@RequestParam("nombre") String nombre,
                                   @RequestParam("cedula") String cedula,
                                   @RequestParam(value = "telefono", required = false) String telefono,
                                   @RequestParam("email") String email,
                                   Model model) {
        Usuario cliente = obtenerClienteAutenticado();
        if (cliente == null) {
            return "redirect:/login";
        }

        boolean duplicado = false;
        if (cedula != null && !cedula.isBlank()) {
            Optional<Usuario> porCedula = usuarioDao.findByCedula(cedula.trim());
            if (porCedula.isPresent() && !porCedula.get().getIdUsuario().equals(cliente.getIdUsuario())) {
                duplicado = true;
            }
        }
        if (!duplicado && email != null && !email.isBlank()) {
            Optional<Usuario> porEmail = usuarioDao.findByEmail(email.trim());
            if (porEmail.isPresent() && !porEmail.get().getIdUsuario().equals(cliente.getIdUsuario())) {
                duplicado = true;
            }
        }

        if (duplicado) {
            model.addAttribute("cliente", cliente);
            model.addAttribute("error", "La cédula o el correo ya están registrados por otra cuenta.");
            return "cliente/perfil";
        }

        if (nombre != null) cliente.setNombre(nombre.trim());
        if (cedula != null) cliente.setCedula(cedula.trim());
        cliente.setTelefono(telefono != null ? telefono.trim() : null);
        if (email != null) cliente.setEmail(email.trim().toLowerCase());

        usuarioDao.save(cliente);
        model.addAttribute("redirectMessage", "tus datos se actualizaron correctamente");
        return "redirect:/cliente/perfil?guardado=true";
    }

}