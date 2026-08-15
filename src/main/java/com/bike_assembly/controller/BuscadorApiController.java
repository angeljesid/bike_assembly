package com.bike_assembly.controller; 

import com.bike_assembly.domain.Producto;
import com.bike_assembly.repository.ProductoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/buscar") // Ruta maestra para todas las búsquedas
public class BuscadorApiController {

    @Autowired
    private ProductoDao productoDao;

    // Endpoint específico para inventario/productos
    @GetMapping("/productos")
    public List<Producto> buscarProductos(@RequestParam(value = "termino", required = false) String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return productoDao.findAll(); 
        }
        return productoDao.buscarPorNombreOMarca(termino);
    }
    
    /* 
     * Más adelante, cuando necesites buscar otras cosas, 
     * solo agregas los métodos aquí abajo. Ejemplo:
     * 
     * @GetMapping("/clientes")
     * public List<Cliente> buscarClientes(...) { ... }
     * 
     * @GetMapping("/proveedores")
     * public List<Proveedor> buscarProveedores(...) { ... }
     */
}