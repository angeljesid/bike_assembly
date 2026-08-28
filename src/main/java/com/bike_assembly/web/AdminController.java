package com.bike_assembly.web;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.domain.Venta;
import com.bike_assembly.repository.ProductoDao;
import com.bike_assembly.services.producto.IProductoService;
import com.bike_assembly.services.proveedor.IProveedorService;
import com.bike_assembly.services.venta.IVentaService;
import com.bike_assembly.repository.UsuarioDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductoDao productoDao;

    @Autowired
    private IProveedorService proveedorService;

    @Autowired
    private UsuarioDao usuarioDao;
    
    @Autowired
    private IProductoService productoService;

    @Autowired
    private IVentaService ventaService;

    @Autowired
    private com.bike_assembly.repository.OrganizacionDao organizacionDao;

    @Autowired
    private com.bike_assembly.services.empleado.IEmpleadoService empleadoService;

    AdminController(ProductoDao productoDao) {
        this.productoDao = productoDao;
    }

    @GetMapping({"", "/", "/inicio", "/dashboard"})
    public String inicioAdmin(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usernameOrEmail = auth.getName();
        
        Usuario admin = usuarioDao.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElse(null);
        model.addAttribute("admin", admin);
        
        if (admin != null && admin.getOrganizacion() != null) {
            model.addAttribute("nombreTienda", admin.getOrganizacion().getNombre());
        } else {
            model.addAttribute("nombreTienda", "Mi Taller");
        }

        return "admin/inicio";
    }

    @GetMapping("/ventas")
    public String listarVentasAdmin(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usernameOrEmail = auth.getName();
        Usuario admin = usuarioDao.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElse(null);

        List<Venta> todasLasVentas = ventaService.listarHistorialVentasAdmin();
        
        if (admin != null && admin.getOrganizacion() != null) {
            Long orgId = admin.getOrganizacion().getIdOrganizacion();
            todasLasVentas = todasLasVentas.stream().filter(v -> {
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
        } else {
            todasLasVentas = List.of();
        }

        List<Venta> ventasFiltradas = todasLasVentas.stream()
                .filter(v -> "COMPLETADA".equalsIgnoreCase(v.getEstado()))
                .collect(Collectors.toList());
        model.addAttribute("ventas", ventasFiltradas);
        return "admin/historial-admin";
    }

    @GetMapping("/producto")
    public String listarProductos(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);

        if (admin != null && admin.getOrganizacion() != null) {
            model.addAttribute("productos", productoDao.findByOrganizacion(admin.getOrganizacion()));
        } else {
            model.addAttribute("productos", List.of());
        }
        return "admin/producto";
    }
    
    @GetMapping("/producto/nuevo")
    public String nuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);
        
        if (admin != null && admin.getOrganizacion() != null) {
            model.addAttribute("proveedores", proveedorService.listarProveedores().stream()
                .filter(p -> p.getOrganizacion() != null && p.getOrganizacion().getIdOrganizacion().equals(admin.getOrganizacion().getIdOrganizacion()))
                .collect(Collectors.toList()));
        } else {
            model.addAttribute("proveedores", List.of());
        }
            
        return "admin/formulario-producto";
    }

    @GetMapping("/producto/editar/{id}")
    public String editarProducto(@PathVariable("id") Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);
        
        Producto producto = new Producto();
        producto.setIdProducto(id);
        producto = productoService.encontrarProducto(producto);
        
        if (admin == null || admin.getOrganizacion() == null || producto == null || producto.getOrganizacion() == null || !producto.getOrganizacion().getIdOrganizacion().equals(admin.getOrganizacion().getIdOrganizacion())) {
            return "redirect:/admin/producto";
        }
        
        model.addAttribute("producto", producto);
        
        model.addAttribute("proveedores", proveedorService.listarProveedores().stream()
            .filter(p -> p.getOrganizacion() != null && p.getOrganizacion().getIdOrganizacion().equals(admin.getOrganizacion().getIdOrganizacion()))
            .collect(Collectors.toList()));
            
        return "admin/formulario-producto";
    }

    @GetMapping("/producto/eliminar/{id}")
    public String eliminarProducto(@PathVariable("id") Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);
        
        Producto producto = new Producto();
        producto.setIdProducto(id);
        Producto productoExistente = productoService.encontrarProducto(producto);
        
        if (admin != null && admin.getOrganizacion() != null && productoExistente != null && productoExistente.getOrganizacion() != null && productoExistente.getOrganizacion().getIdOrganizacion().equals(admin.getOrganizacion().getIdOrganizacion())) {
            productoService.eliminar(productoExistente);
        }
        
        return "redirect:/admin/producto";
    }

    @PostMapping("/producto/guardar")
    public String guardarProducto(Producto producto, @RequestParam(value = "file", required = false) MultipartFile imagen) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);

        if (admin != null && admin.getOrganizacion() != null) {
            producto.setOrganizacion(admin.getOrganizacion());
        }

        if (imagen != null && !imagen.isEmpty()) {
            Path directorioImagenes = Paths.get("uploads");
            String rutaAbsoluta = directorioImagenes.toFile().getAbsolutePath();

            try {
                if (!Files.exists(directorioImagenes)) {
                    Files.createDirectories(directorioImagenes);
                }
                byte[] bytesImg = imagen.getBytes();
                Path rutaCompleta = Paths.get(rutaAbsoluta + "/" + imagen.getOriginalFilename());
                Files.write(rutaCompleta, bytesImg);
                producto.setImagen(imagen.getOriginalFilename());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        productoService.guardar(producto);
        return "redirect:/admin/producto";
    }

    @GetMapping("/configuracion")
    public String configuracionNegocio(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);

        if (admin != null && admin.getOrganizacion() != null) {
            model.addAttribute("admin", admin);
            model.addAttribute("organizacion", admin.getOrganizacion());
            return "admin/configuracion";
        }
        
        return "redirect:/admin";
    }

    @PostMapping("/configuracion/guardar")
    public String guardarConfiguracion(
            @RequestParam("direccion") String direccion,
            @RequestParam("telefono") String telefono) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);

        if (admin != null && admin.getOrganizacion() != null) {
            com.bike_assembly.domain.Organizacion org = admin.getOrganizacion();
            org.setDireccion(direccion);
            org.setTelefono(telefono);
            
            organizacionDao.save(org);
        }

        return "redirect:/admin/configuracion?exito=true";
    }

    @GetMapping("/empleados")
    public String listarEmpleados(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);

        if (admin != null && admin.getOrganizacion() != null) {
            model.addAttribute("empleados", empleadoService.listarEmpleadosPorOrganizacion(admin.getOrganizacion()));
        } else {
            model.addAttribute("empleados", List.of());
        }
        return "admin/empleados";
    }

    @PostMapping("/empleados/guardar")
    public String guardarEmpleado(Usuario empleado, @RequestParam("rolStr") String rolStr, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);

        if (admin != null && admin.getOrganizacion() != null) {
            Rol rol = Rol.valueOf(rolStr);
            try {
                if (empleado.getIdUsuario() == null) {
                    Organizacion org = admin.getOrganizacion();
                    long cantidadOperarios = usuarioDao.countByOrganizacionAndRolAndActivoTrue(org, Rol.OPERARIO);
                    long cantidadCajeros = usuarioDao.countByOrganizacionAndRolAndActivoTrue(org, Rol.CAJA);
                    long totalEmpleados = cantidadOperarios + cantidadCajeros;

                    int limite = 2;
                    if (org.getTipo() != null) {
                        switch (org.getTipo()) {
                            case "Taller Pequeño": limite = 4; break;
                            case "Taller Mediano": limite = 6; break;
                            case "Taller Grande": limite = 12; break;
                        }
                    }

                    if (totalEmpleados >= limite) {
                        redirectAttributes.addFlashAttribute("alertaPlan", "Aumenta el plan del taller para crear más empleados.");
                        redirectAttributes.addAttribute("error", "limite");
                        return "redirect:/admin/empleados";
                    }
                }
                empleadoService.guardarEmpleado(admin, empleado, rol);
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("alertaPlan", "Aumenta el plan del taller. " + e.getMessage());
                redirectAttributes.addAttribute("error", "limite");
                return "redirect:/admin/empleados";
            }
        }
        return "redirect:/admin/empleados?exito=true";
    }

    @GetMapping("/empleados/eliminar/{id}")
    public String eliminarEmpleado(@PathVariable("id") Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);

        if (admin != null && admin.getOrganizacion() != null) {
            empleadoService.eliminarEmpleado(id, admin.getOrganizacion());
        }
        return "redirect:/admin/empleados";
    }
}