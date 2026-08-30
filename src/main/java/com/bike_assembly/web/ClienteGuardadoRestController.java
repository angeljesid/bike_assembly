package com.bike_assembly.web;

import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.ProductoGuardado;
import com.bike_assembly.domain.ProductoCatalogoDTO;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.ProductoDao;
import com.bike_assembly.security.SessionUtils;
import com.bike_assembly.services.cliente.ClienteGuardadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cliente/api/guardados")
public class ClienteGuardadoRestController {

    @Autowired
    private ClienteGuardadoService guardadoService;

    @Autowired
    private ProductoDao productoDao;

    @Autowired
    private SessionUtils sessionUtils;

    private Usuario obtenerClienteAutenticado() {
        return sessionUtils.usuarioActual();
    }

    private Producto obtenerProducto(Long idProducto) {
        if (idProducto == null) {
            return null;
        }
        return productoDao.findById(idProducto).orElse(null);
    }

    @GetMapping
    public ResponseEntity<List<ProductoCatalogoDTO>> listarGuardados() {
        Usuario cliente = obtenerClienteAutenticado();
        if (cliente == null) {
            return ResponseEntity.status(401).body(List.of());
        }
        List<ProductoCatalogoDTO> items = guardadoService.listarPorUsuario(cliente).stream()
                .map(ProductoGuardado::getProducto)
                .filter(p -> p != null && !Boolean.TRUE.equals(p.getEliminado()))
                .map(ProductoCatalogoDTO::new)
                .toList();
        return ResponseEntity.ok(items);
    }

    @PostMapping("/{idProducto}")
    public ResponseEntity<?> guardar(@PathVariable Long idProducto) {
        Usuario cliente = obtenerClienteAutenticado();
        if (cliente == null) {
            return ResponseEntity.status(401).build();
        }
        Producto producto = obtenerProducto(idProducto);
        if (producto == null || Boolean.TRUE.equals(producto.getEliminado())) {
            return ResponseEntity.badRequest().body(Map.of("error", "El producto no existe."));
        }
        guardadoService.guardar(cliente, producto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{idProducto}")
    public ResponseEntity<?> quitar(@PathVariable Long idProducto) {
        Usuario cliente = obtenerClienteAutenticado();
        if (cliente == null) {
            return ResponseEntity.status(401).build();
        }
        Producto producto = obtenerProducto(idProducto);
        if (producto == null) {
            return ResponseEntity.ok().build();
        }
        guardadoService.quitar(cliente, producto);
        return ResponseEntity.ok().build();
    }
}
