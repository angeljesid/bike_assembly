package com.bike_assembly.web;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.domain.Venta;
import com.bike_assembly.repository.ProductoDao;
import com.bike_assembly.security.SessionUtils;
import com.bike_assembly.services.producto.IProductoService;
import com.bike_assembly.services.proveedor.IProveedorService;
import com.bike_assembly.services.venta.IVentaService;
import com.bike_assembly.repository.UsuarioDao;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private SessionUtils sessionUtils;

    AdminController(ProductoDao productoDao) {
        this.productoDao = productoDao;
    }

    private Usuario obtenerAdminAutenticado() {
        return sessionUtils.usuarioActual();
    }

    @GetMapping({"", "/", "/inicio", "/dashboard"})
    public String inicioAdmin(Model model) {
        Usuario admin = obtenerAdminAutenticado();
        model.addAttribute("admin", admin);
        
        if (admin != null && admin.getOrganizacion() != null) {
            model.addAttribute("nombreTienda", admin.getOrganizacion().getNombre());

            List<Producto> todos = productoDao.findByOrganizacionAndEliminadoFalse(admin.getOrganizacion());
            List<Producto> productosAgotados = productoDao.findByOrganizacionAndStockLessThanEqualAndEliminadoFalse(admin.getOrganizacion(), 0);
            List<Producto> productosStockBajo = productoDao.findByOrganizacionAndStockLessThanEqualAndEliminadoFalse(admin.getOrganizacion(), 5);
            long operariosActivos = usuarioDao.countByOrganizacionAndRolAndActivoTrue(admin.getOrganizacion(), Rol.OPERARIO);

            model.addAttribute("totalComponentes", todos.size());
            model.addAttribute("productosAgotados", productosAgotados);
            model.addAttribute("productosStockBajo", productosStockBajo);
            model.addAttribute("operariosActivos", operariosActivos);
        } else {
            model.addAttribute("nombreTienda", "Mi Taller");
            model.addAttribute("totalComponentes", 0);
            model.addAttribute("productosAgotados", List.of());
            model.addAttribute("productosStockBajo", List.of());
            model.addAttribute("operariosActivos", 0L);
        }

        return "admin/inicio";
    }

    @GetMapping("/ventas")
    public String listarVentasAdmin(Model model) {
        Usuario admin = obtenerAdminAutenticado();

        List<Venta> ventasFiltradas = admin != null && admin.getOrganizacion() != null
                ? ventaService.listarVentasPorOrganizacion(admin.getOrganizacion().getIdOrganizacion(), "COMPLETADA")
                : List.of();
        model.addAttribute("ventas", ventasFiltradas);
        return "admin/historial-admin";
    }

    @GetMapping("/producto")
    public String listarProductos(Model model) {
        Usuario admin = obtenerAdminAutenticado();

        if (admin != null && admin.getOrganizacion() != null) {
            List<Producto> productos = productoDao.findByOrganizacionAndEliminadoFalse(admin.getOrganizacion());
            model.addAttribute("productos", productos);
            java.util.Map<Long, Integer> numeracion = new java.util.LinkedHashMap<>();
            int idx = 1;
            for (Producto p : productos) {
                numeracion.put(p.getIdProducto(), idx++);
            }
            model.addAttribute("numeracionProductos", numeracion);
        } else {
            model.addAttribute("productos", List.of());
            model.addAttribute("numeracionProductos", java.util.Map.of());
        }
        return "admin/producto";
    }
    
    @GetMapping("/producto/nuevo")
    public String nuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());
        Usuario admin = obtenerAdminAutenticado();
        
        if (admin != null && admin.getOrganizacion() != null) {
            model.addAttribute("proveedores", proveedorService.listarProveedoresPorOrganizacion(admin.getOrganizacion()));
        } else {
            model.addAttribute("proveedores", List.of());
        }
            
        return "admin/formulario-producto";
    }

    @GetMapping("/producto/editar/{id}")
    public String editarProducto(@PathVariable("id") Long id, Model model) {
        Usuario admin = obtenerAdminAutenticado();
        
        Producto producto = new Producto();
        producto.setIdProducto(id);
        producto = productoService.encontrarProducto(producto);
        
        if (admin == null || admin.getOrganizacion() == null || producto == null
                || !productoService.productoPerteneceAOrganizacion(producto, admin.getOrganizacion().getIdOrganizacion())) {
            return "redirect:/admin/producto";
        }
        
        model.addAttribute("producto", producto);
        if (producto.getUnidadesPublicadas() == null || producto.getUnidadesPublicadas() == 0) {
            if (Boolean.TRUE.equals(producto.getPublicado()) && producto.getStock() != null && producto.getStock() > 0) {
                producto.setUnidadesPublicadas(producto.getStock());
            } else {
                producto.setUnidadesPublicadas(0);
            }
        }
        
        model.addAttribute("proveedores", proveedorService.listarProveedoresPorOrganizacion(admin.getOrganizacion()));
            
        return "admin/formulario-producto";
    }

    @GetMapping("/producto/eliminar/{id}")
    public String eliminarProducto(@PathVariable("id") Long id) {
        Usuario admin = obtenerAdminAutenticado();
        
        Producto producto = new Producto();
        producto.setIdProducto(id);
        Producto productoExistente = productoService.encontrarProducto(producto);
        
        if (admin != null && admin.getOrganizacion() != null && productoExistente != null
                && productoService.productoPerteneceAOrganizacion(productoExistente, admin.getOrganizacion().getIdOrganizacion())) {
            productoExistente.setEliminado(true);
            productoExistente.setPublicado(false);
            productoExistente.setUnidadesPublicadas(0);
            productoService.guardar(productoExistente);
        }
        
        return "redirect:/admin/producto";
    }

    @PostMapping("/producto/guardar")
    public String guardarProducto(Producto producto, @RequestParam(value = "file", required = false) MultipartFile imagen) {
        Usuario admin = obtenerAdminAutenticado();

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

        Integer stock = producto.getStock() != null ? producto.getStock() : 0;
        Integer unidades = producto.getUnidadesPublicadas() != null ? producto.getUnidadesPublicadas() : 0;
        if (unidades < 0) {
            unidades = 0;
        }
        if (unidades > stock) {
            unidades = stock;
        }
        producto.setUnidadesPublicadas(unidades);
        producto.setPublicado(unidades > 0);

        if (producto.getIdProducto() != null) {
            Producto existente = productoService.encontrarProducto(
                    new Producto() {{ setIdProducto(producto.getIdProducto()); }});
            if (existente != null) {
                producto.setEliminado(Boolean.TRUE.equals(existente.getEliminado()));
            }
        }
        if (producto.getEliminado() == null) {
            producto.setEliminado(false);
        }

        productoService.guardar(producto);
        return "redirect:/admin/producto";
    }

    @GetMapping("/configuracion")
    public String configuracionNegocio(Model model) {
        Usuario admin = obtenerAdminAutenticado();

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

        Usuario admin = obtenerAdminAutenticado();

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
        Usuario admin = obtenerAdminAutenticado();

        if (admin != null && admin.getOrganizacion() != null) {
            model.addAttribute("admin", admin);
            model.addAttribute("empleados", empleadoService.listarEmpleadosPorOrganizacion(admin.getOrganizacion()));
            model.addAttribute("empleadosSuspendidos", empleadoService.listarEmpleadosSuspendidosPorOrganizacion(admin.getOrganizacion()));
        } else {
            model.addAttribute("empleados", List.of());
            model.addAttribute("empleadosSuspendidos", List.of());
        }
        return "admin/empleados";
    }

    @PostMapping("/empleados/guardar")
    public String guardarEmpleado(Usuario empleado, @RequestParam("rolStr") String rolStr, RedirectAttributes redirectAttributes) {
        Usuario admin = obtenerAdminAutenticado();

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

    @GetMapping("/empleados/suspender/{id}")
    public String suspenderEmpleado(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Usuario admin = obtenerAdminAutenticado();

        if (admin != null && admin.getOrganizacion() != null) {
            try {
                empleadoService.suspenderEmpleado(id, admin.getOrganizacion());
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("alertaPlan", e.getMessage());
                return "redirect:/admin/empleados";
            }
        }
        return "redirect:/admin/empleados?exito=true";
    }

    @GetMapping("/empleados/activar/{id}")
    public String activarEmpleado(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Usuario admin = obtenerAdminAutenticado();

        if (admin != null && admin.getOrganizacion() != null) {
            try {
                boolean activado = empleadoService.activarEmpleado(id, admin.getOrganizacion());
                if (!activado) {
                    redirectAttributes.addFlashAttribute("alertaPlan",
                            "No puedes activar más cuentas de lo permitido por tu plan actual. Aumenta el plan del taller para agregar más empleados.");
                    return "redirect:/admin/empleados";
                }
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("alertaPlan", e.getMessage());
                return "redirect:/admin/empleados";
            }
        }
        return "redirect:/admin/empleados?exito=true";
    }
}