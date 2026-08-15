package com.bike_assembly.web;

import com.bike_assembly.domain.Proveedor;
import com.bike_assembly.services.proveedor.IProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/proveedor")
public class ProveedorController {

    @Autowired
    private IProveedorService proveedorService;

    @GetMapping({"", "/"})
    public String listarProveedores(Model model) {
        model.addAttribute("proveedores", proveedorService.listarProveedores());
        return "admin/proveedor"; // Vista que crearemos luego
    }

    @GetMapping("/nuevo")
    public String nuevoProveedor(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        return "admin/formulario-proveedor";
    }

    @PostMapping("/guardar")
    public String guardarProveedor(Proveedor proveedor) {
        proveedorService.guardar(proveedor);
        return "redirect:/admin/proveedor";
    }

    @GetMapping("/editar/{id}")
    public String editarProveedor(@PathVariable("id") Long id, Model model) {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(id);
        proveedor = proveedorService.encontrarProveedor(proveedor);
        model.addAttribute("proveedor", proveedor);
        return "admin/formulario-proveedor";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProveedor(@PathVariable("id") Long id) {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(id);
        proveedorService.eliminar(proveedor);
        return "redirect:/admin/proveedor";
    }
}