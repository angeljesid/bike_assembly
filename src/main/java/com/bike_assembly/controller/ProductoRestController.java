package com.bike_assembly.controller;

import com.bike_assembly.domain.Producto; 
import com.bike_assembly.repository.ProductoDao; // Usamos tu DAO directamente

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

    @Autowired
    private ProductoDao productoDao; // Inyectamos el DAO en lugar del Service

    @GetMapping
    public ResponseEntity<List<Producto>> listarProductosParaBuscador() {
        // Al usar el DAO, findAll() siempre existe en Spring Boot
        List<Producto> productos = (List<Producto>) productoDao.findAll(); 
        
        return ResponseEntity.ok(productos);
    }
}