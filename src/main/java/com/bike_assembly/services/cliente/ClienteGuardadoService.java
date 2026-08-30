package com.bike_assembly.services.cliente;

import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.ProductoGuardado;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.ProductoGuardadoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteGuardadoService {

    @Autowired
    private ProductoGuardadoDao productoGuardadoDao;

    @Transactional(readOnly = true)
    public List<ProductoGuardado> listarPorUsuario(Usuario usuario) {
        return productoGuardadoDao.findByUsuario(usuario);
    }

    @Transactional
    public boolean guardar(Usuario usuario, Producto producto) {
        if (productoGuardadoDao.existsByUsuarioAndProducto(usuario, producto)) {
            return false;
        }
        ProductoGuardado guardado = new ProductoGuardado();
        guardado.setUsuario(usuario);
        guardado.setProducto(producto);
        productoGuardadoDao.save(guardado);
        return true;
    }

    @Transactional
    public boolean quitar(Usuario usuario, Producto producto) {
        if (!productoGuardadoDao.existsByUsuarioAndProducto(usuario, producto)) {
            return false;
        }
        productoGuardadoDao.deleteByUsuarioAndProducto(usuario, producto);
        return true;
    }
}
