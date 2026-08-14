package com.bike_assembly.repository;

import com.bike_assembly.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductoDao extends JpaRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p WHERE p.tipo = :tipoElegido OR p.tipo = 'U'")
    List<Producto> findComponentesCompatibles(@Param("tipoElegido") String tipoElegido);
}