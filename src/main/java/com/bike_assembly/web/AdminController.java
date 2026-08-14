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
        
        // Buscamos de forma segura por email
        Usuario admin = usuarioDao.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElse(null);

        model.addAttribute("admin", admin);
        
        // Verificación limpia del if sin caracteres raros
        if (admin != null && admin.getOrganizacion() != null) {
            model.addAttribute("nombreTienda", admin.getOrganizacion().getNombre());
        } else {
            model.addAttribute("nombreTienda", "Mi Taller");
        }

        return "admin/inicio"; 
    }

    @GetMapping("/producto")
    public String listarproductos(Model model) {
        model.addAttribute("productos", productoService.listarProductos());
        return "admin/producto";
    }
    
    @GetMapping("/producto/nuevo")
    public String nuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());
        return "admin/formulario-suministro";
    }

    @PostMapping("/Producto/guardar")
    public String guardarProducto(Producto producto) {
        productoService.guardar(producto);
        return "redirect:/admin/suministros";
    }

    @GetMapping("/Producto/editar/{id}")
    public String editarProducto(@PathVariable("id") Long id, Model model) {
        Producto producto = new Producto();
        producto.setIdProducto(id);
        producto = productoService.encontrarProducto(producto);
        
        model.addAttribute("producto", producto);
        return "admin/formulario-suministro";
    }

    @GetMapping("/producto/eliminar/{id}")
    public String eliminarProducto(@PathVariable("id") Long id) {
        Producto producto = new Producto();
        producto.setIdProducto(id);
        productoService.eliminar(producto);
        
        return "redirect:/admin/producto";
}
    }