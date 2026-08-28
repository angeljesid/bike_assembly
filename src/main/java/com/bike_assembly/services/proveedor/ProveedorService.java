package com.bike_assembly.services.proveedor;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Proveedor;
import com.bike_assembly.repository.ProveedorDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProveedorService implements IProveedorService {

    @Autowired
    private ProveedorDao proveedorDao;

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> listarProveedores() {
        return proveedorDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> listarProveedoresPorOrganizacion(Organizacion organizacion) {
        return proveedorDao.findByOrganizacion(organizacion);
    }

    @Override
    @Transactional
    public void guardar(Proveedor proveedor) {
        proveedorDao.save(proveedor);
    }

    @Override
    @Transactional
    public void eliminar(Proveedor proveedor) {
        proveedorDao.delete(proveedor);
    }

    @Override
    @Transactional(readOnly = true)
    public Proveedor encontrarProveedor(Proveedor proveedor) {
        return proveedorDao.findById(proveedor.getIdProveedor()).orElse(null);
    }

    @Override
    public boolean proveedorPerteneceAOrganizacion(Proveedor proveedor, Long idOrganizacion) {
        return proveedor != null
                && idOrganizacion != null
                && proveedor.getOrganizacion() != null
                && proveedor.getOrganizacion().getIdOrganizacion().equals(idOrganizacion);
    }
}