package com.bike_assembly.controller;

import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.Proveedor;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.ProductoDao;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.services.proveedor.IProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class BuscadorProveedorRestController {

    @Autowired
    private IProveedorService proveedorService;

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private ProductoDao productoDao;

    @GetMapping("/api/buscar/proveedores")
    public ResponseEntity<List<Proveedor>> listarProveedoresAPI() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);

        if (admin != null && admin.getOrganizacion() != null) {
            List<Proveedor> proveedores = proveedorService.listarProveedoresPorOrganizacion(admin.getOrganizacion());
            return ResponseEntity.ok(proveedores);
        }
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/api/buscar/productos")
    public ResponseEntity<List<Producto>> buscarProductosAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);

        if (admin != null && admin.getOrganizacion() != null) {
            return ResponseEntity.ok(productoDao.findByOrganizacion(admin.getOrganizacion()));
        }
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/api/productos")
    public ResponseEntity<List<Producto>> buscarProductosCliente() {
        return ResponseEntity.ok(productoDao.findByPublicadoTrue());
    }
}