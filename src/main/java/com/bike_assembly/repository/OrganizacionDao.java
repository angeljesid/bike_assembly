package com.bike_assembly.repository;

import com.bike_assembly.domain.Organizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface OrganizacionDao extends JpaRepository<Organizacion, Long> {

    @Query("SELECT COALESCE(SUM(o.precioPlan), 0) FROM Organizacion o WHERE o.activo = true OR o.activo IS NULL")
    BigDecimal sumarPrecioPlanesActivos();
}
