package com.bike_assembly.services.producto;

import com.bike_assembly.domain.Producto;
import java.util.List;

public interface IProductoService {
    List<Producto> listarProductos();
    List<Producto> obtenerCompatibles(String tipoElegido);
    void guardar(Producto producto);
    void eliminar(Producto producto);
    Producto encontrarProducto(Producto producto);
    boolean productoPerteneceAOrganizacion(Producto producto, Long idOrganizacion);
    boolean productoTieneVentas(Producto producto);
}