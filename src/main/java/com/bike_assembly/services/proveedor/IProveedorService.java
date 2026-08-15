package com.bike_assembly.services.proveedor;

import com.bike_assembly.domain.Proveedor;
import java.util.List;

public interface IProveedorService {
    List<Proveedor> listarProveedores();
    void guardar(Proveedor proveedor);
    void eliminar(Proveedor proveedor);
    Proveedor encontrarProveedor(Proveedor proveedor);
}