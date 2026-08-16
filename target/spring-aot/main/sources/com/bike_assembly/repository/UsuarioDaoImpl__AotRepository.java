package com.bike_assembly.repository;

import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.lang.String;
import java.util.List;
import java.util.Optional;
import org.springframework.aot.generate.Generated;
import org.springframework.data.jpa.repository.aot.AotRepositoryFragmentSupport;
import org.springframework.data.jpa.repository.query.QueryEnhancerSelector;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;

/**
 * AOT generated JPA repository implementation for {@link UsuarioDao}.
 */
@Generated
public class UsuarioDaoImpl__AotRepository extends AotRepositoryFragmentSupport {
  private final RepositoryFactoryBeanSupport.FragmentCreationContext context;

  private final EntityManager entityManager;

  public UsuarioDaoImpl__AotRepository(EntityManager entityManager,
      RepositoryFactoryBeanSupport.FragmentCreationContext context) {
    super(QueryEnhancerSelector.DEFAULT_SELECTOR, context);
    this.entityManager = entityManager;
    this.context = context;
  }

  /**
   * AOT generated implementation of {@link UsuarioDao#existsByRol(com.bike_assembly.domain.Rol)}.
   */
  public boolean existsByRol(Rol rol) {
    String queryString = "SELECT u.idUsuario FROM Usuario u WHERE u.rol = :rol";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("rol", rol);
    query.setMaxResults(1);

    return !query.getResultList().isEmpty();
  }

  /**
   * AOT generated implementation of {@link UsuarioDao#findByEmail(java.lang.String)}.
   */
  public Optional<Usuario> findByEmail(String email) {
    String queryString = "SELECT u FROM Usuario u WHERE u.email = :email";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("email", email);

    return Optional.ofNullable((Usuario) convertOne(query.getSingleResultOrNull(), false, Usuario.class));
  }

  /**
   * AOT generated implementation of {@link UsuarioDao#findByResetPasswordToken(java.lang.String)}.
   */
  public Optional<Usuario> findByResetPasswordToken(String token) {
    String queryString = "SELECT u FROM Usuario u WHERE u.resetPasswordToken = :token";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("token", token);

    return Optional.ofNullable((Usuario) convertOne(query.getSingleResultOrNull(), false, Usuario.class));
  }

  /**
   * AOT generated implementation of {@link UsuarioDao#findByRol(com.bike_assembly.domain.Rol)}.
   */
  public List<Usuario> findByRol(Rol rol) {
    String queryString = "SELECT u FROM Usuario u WHERE u.rol = :rol";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("rol", rol);

    return (List<Usuario>) query.getResultList();
  }

  /**
   * AOT generated implementation of {@link UsuarioDao#findByUsername(java.lang.String)}.
   */
  public Optional<Usuario> findByUsername(String username) {
    String queryString = "SELECT u FROM Usuario u WHERE u.username = :username";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("username", username);

    return Optional.ofNullable((Usuario) convertOne(query.getSingleResultOrNull(), false, Usuario.class));
  }

  /**
   * AOT generated implementation of {@link UsuarioDao#findByUsernameOrEmail(java.lang.String,java.lang.String)}.
   */
  public Optional<Usuario> findByUsernameOrEmail(String username, String email) {
    String queryString = "SELECT u FROM Usuario u WHERE u.username = :username OR u.email = :email";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("username", username);
    query.setParameter("email", email);

    return Optional.ofNullable((Usuario) convertOne(query.getSingleResultOrNull(), false, Usuario.class));
  }
}
