package com.bike_assembly.repository;

import com.bike_assembly.domain.Producto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.lang.String;
import java.util.List;
import org.springframework.aot.generate.Generated;
import org.springframework.data.jpa.repository.aot.AotRepositoryFragmentSupport;
import org.springframework.data.jpa.repository.query.QueryEnhancerSelector;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.query.Param;

/**
 * AOT generated JPA repository implementation for {@link ProductoDao}.
 */
@Generated
public class ProductoDaoImpl__AotRepository extends AotRepositoryFragmentSupport {
  private final RepositoryFactoryBeanSupport.FragmentCreationContext context;

  private final EntityManager entityManager;

  public ProductoDaoImpl__AotRepository(EntityManager entityManager,
      RepositoryFactoryBeanSupport.FragmentCreationContext context) {
    super(QueryEnhancerSelector.DEFAULT_SELECTOR, context);
    this.entityManager = entityManager;
    this.context = context;
  }

  /**
   * AOT generated implementation of {@link ProductoDao#buscarPorNombreOMarca(java.lang.String)}.
   */
  public List<Producto> buscarPorNombreOMarca(@Param("termino") String termino) {
    String queryString = "SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(p.marca) LIKE LOWER(CONCAT('%', :termino, '%'))";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("termino", termino);

    return (List<Producto>) query.getResultList();
  }

  /**
   * AOT generated implementation of {@link ProductoDao#findComponentesCompatibles(java.lang.String)}.
   */
  public List<Producto> findComponentesCompatibles(@Param("tipoElegido") String tipoElegido) {
    String queryString = "SELECT p FROM Producto p WHERE p.tipo = :tipoElegido OR p.tipo = 'U'";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("tipoElegido", tipoElegido);

    return (List<Producto>) query.getResultList();
  }
}
