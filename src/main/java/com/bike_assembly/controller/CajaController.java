package com.bike_assembly.controller;

import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.VentaRequestDTO;
import com.bike_assembly.repository.ProductoDao;
import com.bike_assembly.services.venta.IVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/caja")
public class CajaController {

    @Autowired
    private ProductoDao productoDao;

    @Autowired
    private IVentaService ventaService;

    @GetMapping({"", "/"})
    public String mostrarCaja() {
        return "admin/caja"; 
    }

    // ARREGLADO: Ahora sí busca el producto en la base de datos
    @GetMapping("/api/buscar-producto")
    @ResponseBody
    public ResponseEntity<?> buscarProductoPorCodigo(@RequestParam("codigo") String codigoBarras) {
        Optional<Producto> producto = productoDao.findByCodigoBarras(codigoBarras);
        
        if (producto.isPresent()) {
            return ResponseEntity.ok(producto.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/api/cobrar")
    @ResponseBody
    public ResponseEntity<?> cobrarVenta(@RequestBody VentaRequestDTO request) {
        Map<String, Object> response = new HashMap<>();
        try {
            ventaService.procesarCobro(request);
            response.put("success", true);
            response.put("mensaje", "Venta registrada con éxito");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}