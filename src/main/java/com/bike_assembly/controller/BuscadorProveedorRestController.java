package com.bike_assembly.controller;

import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.ProductoCatalogoDTO;
import com.bike_assembly.domain.Proveedor;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.ProductoDao;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.security.SessionUtils;
import com.bike_assembly.services.proveedor.IProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    private SessionUtils sessionUtils;

    private Usuario obtenerAdminAutenticado() {
        return sessionUtils.usuarioActual();
    }

    @GetMapping("/api/buscar/proveedores")
    public ResponseEntity<List<Proveedor>> listarProveedoresAPI() {
        Usuario admin = obtenerAdminAutenticado();

        if (admin != null && admin.getOrganizacion() != null) {
            List<Proveedor> proveedores = proveedorService.listarProveedoresPorOrganizacion(admin.getOrganizacion());
            return ResponseEntity.ok(proveedores);
        }
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/api/buscar/productos")
    public ResponseEntity<List<Producto>> buscarProductosAdmin() {
        Usuario admin = obtenerAdminAutenticado();

        if (admin != null && admin.getOrganizacion() != null) {
            return ResponseEntity.ok(productoDao.findByOrganizacionAndEliminadoFalse(admin.getOrganizacion()));
        }
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/api/productos")
    public ResponseEntity<List<ProductoCatalogoDTO>> buscarProductosCliente() {
        List<ProductoCatalogoDTO> publicados = productoDao.findProductosDiponiblesParaCliente().stream()
                .map(ProductoCatalogoDTO::new)
                .toList();
        return ResponseEntity.ok(publicados);
    }
}