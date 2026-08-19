package com.bike_assembly.web;

import com.bike_assembly.domain.Proveedor;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.services.proveedor.IProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/proveedor")
public class ProveedorController {

    @Autowired
    private IProveedorService proveedorService;

    @Autowired
    private UsuarioDao usuarioDao;

    @GetMapping({"", "/"})
    public String listarProveedores(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);

        if (admin != null && admin.getOrganizacion() != null) {
            List<Proveedor> proveedores = proveedorService.listarProveedoresPorOrganizacion(admin.getOrganizacion());
            model.addAttribute("proveedores", proveedores);
        } else {
            model.addAttribute("proveedores", List.of());
        }
        return "admin/proveedor";
    }

    @GetMapping("/nuevo")
    public String nuevoProveedor(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        return "admin/formulario-proveedor";
    }

    @GetMapping("/editar/{id}")
    public String editarProveedor(@PathVariable("id") Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);

        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(id);
        Proveedor proveedorExistente = proveedorService.encontrarProveedor(proveedor);

        if (admin == null || admin.getOrganizacion() == null || proveedorExistente == null || proveedorExistente.getOrganizacion() == null || !proveedorExistente.getOrganizacion().getIdOrganizacion().equals(admin.getOrganizacion().getIdOrganizacion())) {
            return "redirect:/admin/proveedor";
        }

        model.addAttribute("proveedor", proveedorExistente);
        return "admin/formulario-proveedor";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProveedor(@PathVariable("id") Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);

        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(id);
        Proveedor proveedorExistente = proveedorService.encontrarProveedor(proveedor);

        if (admin != null && admin.getOrganizacion() != null && proveedorExistente != null && proveedorExistente.getOrganizacion() != null && proveedorExistente.getOrganizacion().getIdOrganizacion().equals(admin.getOrganizacion().getIdOrganizacion())) {
            proveedorService.eliminar(proveedorExistente);
        }

        return "redirect:/admin/proveedor";
    }

    @PostMapping("/guardar")
    public String guardarProveedor(Proveedor proveedor) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);

        if (admin == null || admin.getOrganizacion() == null) {
            return "redirect:/admin/proveedor";
        }

        // Si el proveedor tiene ID, actualizamos el registro existente para no perder relaciones ni la organización
        if (proveedor.getIdProveedor() != null) {
            Proveedor proveedorExistente = proveedorService.encontrarProveedor(proveedor);
            if (proveedorExistente != null && proveedorExistente.getOrganizacion() != null &&
                proveedorExistente.getOrganizacion().getIdOrganizacion().equals(admin.getOrganizacion().getIdOrganizacion())) {
                
                proveedorExistente.setNombre(proveedor.getNombre());
                proveedorExistente.setTipoEstablecimiento(proveedor.getTipoEstablecimiento());
                proveedorExistente.setNumero(proveedor.getNumero());
                proveedorExistente.setCorreo(proveedor.getCorreo());
                proveedorExistente.setDireccion(proveedor.getDireccion());
                
                proveedorService.guardar(proveedorExistente);
            }
        } else {
            // Si es nuevo, le asignamos la organización del admin en sesión y guardamos
            proveedor.setOrganizacion(admin.getOrganizacion());
            proveedorService.guardar(proveedor);
        }

        return "redirect:/admin/proveedor";
    }
}