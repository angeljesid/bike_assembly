package com.bike_assembly.repository;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProveedorDao extends JpaRepository<Proveedor, Long> {
    List<Proveedor> findByOrganizacion(Organizacion organizacion);
}