package com.bike_assembly.web;

import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.services.producto.IProductoService;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioDao usuarioDao;
    
    @Autowired
    private IProductoService productoService;

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

    @GetMapping("/producto")
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoService.listarProductos());
        return "admin/producto";
    }
    
    @GetMapping("/producto/nuevo")
    public String nuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());
        return "admin/formulario-producto";
    }

    @GetMapping("/producto/editar/{id}")
    public String editarProducto(@PathVariable("id") Long id, Model model) {
        Producto producto = new Producto();
        producto.setIdProducto(id);
        producto = productoService.encontrarProducto(producto);
        
        model.addAttribute("producto", producto);
        return "admin/formulario-producto";
    }

    @GetMapping("/producto/eliminar/{id}")
    public String eliminarProducto(@PathVariable("id") Long id) {
        Producto producto = new Producto();
        producto.setIdProducto(id);
        productoService.eliminar(producto);
        
        return "redirect:/admin/producto";
    }

    @PostMapping("/producto/guardar")
    public String guardarProducto(Producto producto, @RequestParam(value = "file", required = false) MultipartFile imagen) {
        
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
}