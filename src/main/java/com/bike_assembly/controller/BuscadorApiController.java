package com.bike_assembly.controller; 

import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.Proveedor;
import com.bike_assembly.repository.ProductoDao;
import com.bike_assembly.repository.ProveedorDao; // Asegúrate de tener tu ProveedorDao inyectado
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buscar")
public class BuscadorApiController {

    private final ProductoDao productoDao;
    private final ProveedorDao proveedorDao;

    public BuscadorApiController(ProductoDao productoDao, ProveedorDao proveedorDao) {
        this.productoDao = productoDao;
        this.proveedorDao = proveedorDao;
    }

    @GetMapping("/productos")
    public ResponseEntity<List<Producto>> buscarProductos() {
        try {
            List<Producto> resultados = productoDao.findAll();
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/proveedores")
    public ResponseEntity<List<Proveedor>> buscarProveedores() {
        try {
            List<Proveedor> resultados = proveedorDao.findAll();
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}