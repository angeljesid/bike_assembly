package com.bike_assembly.repository;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProductoDao extends JpaRepository<Producto, Long> {
    
    @Query("SELECT p FROM Producto p WHERE p.tipo = :tipoElegido OR p.tipo = 'U'")
    List<Producto> findComponentesCompatibles(@Param("tipoElegido") String tipoElegido);
    
    List<Producto> findByOrganizacion(Organizacion organizacion);

    List<Producto> findByOrganizacionAndStockLessThanEqual(Organizacion organizacion, int stock);

    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(p.marca) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Producto> buscarPorNombreOMarca(@Param("termino") String termino);

    Optional<Producto> findByCodigoBarras(String codigoBarras);
    
    Optional<Producto> findByCodigoBarrasAndOrganizacion(String codigoBarras, Organizacion organizacion);
    
    List<Producto> findByPublicadoTrue();
}