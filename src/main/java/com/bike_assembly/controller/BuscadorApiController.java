package com.bike_assembly.controller; 

import com.bike_assembly.domain.Producto;
import com.bike_assembly.repository.ProductoDao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buscar")
public class BuscadorApiController {

    private final ProductoDao productoDao;

    public BuscadorApiController(ProductoDao productoDao) {
        this.productoDao = productoDao;
    }

    @GetMapping("/productos")
    public ResponseEntity<List<Producto>> buscarProductos(@RequestParam(value = "termino", required = false) String termino) {
        try {
            List<Producto> resultados;
            
            if (termino == null || termino.trim().isEmpty()) {
                resultados = productoDao.findAll(); 
            } else {
                resultados = productoDao.buscarPorNombreOMarca(termino.trim());
            }
            
            return ResponseEntity.ok(resultados);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}