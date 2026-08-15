package com.bike_assembly.repository;

import com.bike_assembly.domain.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProveedorDao extends JpaRepository<Proveedor, Long> {
}