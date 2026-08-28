package com.bike_assembly.repository;

import com.bike_assembly.domain.DetalleVenta;
import com.bike_assembly.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleVentaDao extends JpaRepository<DetalleVenta, Long> {

    boolean existsByProducto(Producto producto);
}