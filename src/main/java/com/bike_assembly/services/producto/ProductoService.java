package com.bike_assembly.services.producto;

import com.bike_assembly.domain.Producto;
import com.bike_assembly.repository.ProductoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProductoService implements IProductoService {

    @Autowired
    private ProductoDao productoDao;

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarProductos() {
        return productoDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerCompatibles(String tipoElegido) {
        return productoDao.findComponentesCompatibles(tipoElegido);
    }

    @Override
    @Transactional
    public void guardar(Producto producto) {
        productoDao.save(producto);
    }

    @Override
    @Transactional
    public void eliminar(Producto producto) {
        productoDao.delete(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public Producto encontrarProducto(Producto producto) {
        return productoDao.findById(producto.getIdProducto()).orElse(null);
    }

    @Override
    public boolean productoPerteneceAOrganizacion(Producto producto, Long idOrganizacion) {
        return producto != null
                && idOrganizacion != null
                && producto.getOrganizacion() != null
                && producto.getOrganizacion().getIdOrganizacion().equals(idOrganizacion);
    }
}