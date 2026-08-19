package com.bike_assembly.services.proveedor;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Proveedor;
import java.util.List;

public interface IProveedorService {
    List<Proveedor> listarProveedores();
    List<Proveedor> listarProveedoresPorOrganizacion(Organizacion organizacion);
    void guardar(Proveedor proveedor);
    void eliminar(Proveedor proveedor);
    Proveedor encontrarProveedor(Proveedor proveedor);
}