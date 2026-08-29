package com.bike_assembly.repository;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProductoDao extends JpaRepository<Producto, Long> {
    
    @Query("SELECT p FROM Producto p WHERE (p.tipo = :tipoElegido OR p.tipo = 'U') AND (p.eliminado = false OR p.eliminado IS NULL)")
    List<Producto> findComponentesCompatibles(@Param("tipoElegido") String tipoElegido);
    
    List<Producto> findByOrganizacion(Organizacion organizacion);

    List<Producto> findByOrganizacionAndEliminadoFalse(Organizacion organizacion);

    List<Producto> findByOrganizacionAndStockLessThanEqual(Organizacion organizacion, int stock);

    List<Producto> findByOrganizacionAndStockLessThanEqualAndEliminadoFalse(Organizacion organizacion, int stock);

    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(p.marca) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Producto> buscarPorNombreOMarca(@Param("termino") String termino);

    Optional<Producto> findByCodigoBarras(String codigoBarras);
    
    Optional<Producto> findByCodigoBarrasAndOrganizacion(String codigoBarras, Organizacion organizacion);

    Optional<Producto> findByCodigoBarrasAndOrganizacionAndEliminadoFalse(String codigoBarras, Organizacion organizacion);
    
    List<Producto> findByPublicadoTrue();

    List<Producto> findByPublicadoTrueAndEliminadoFalse();

    @Query("SELECT p FROM Producto p WHERE p.publicado = true AND p.unidadesPublicadas > 0 AND (p.eliminado = false OR p.eliminado IS NULL)")
    List<Producto> findProductosDiponiblesParaCliente();
}