package com.bike_assembly.repository;

import com.bike_assembly.domain.Usuario;
import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Rol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface UsuarioDao extends JpaRepository<Usuario, Long> {

    long countByOrganizacionAndRolAndActivoTrue(Organizacion organizacion, Rol rol);

    List<Usuario> findByOrganizacion(Organizacion organizacion);
    List<Usuario> findByOrganizacionAndActivoTrue(Organizacion organizacion);
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByResetPasswordToken(String token);
    @Query("SELECT u FROM Usuario u WHERE u.username = :username OR u.email = :email")
    Optional<Usuario> findByUsernameOrEmail(String username, String email);
    Optional<Usuario> findByCedula(String cedula);
    
    List<Usuario> findByNombreContainingIgnoreCaseOrCedulaContainingIgnoreCaseAndRol(String nombre, String cedula, Rol rol);
    
    boolean existsByRol(Rol rol);
    @Query("SELECT u FROM Usuario u WHERE u.rol = :rol")
    List<Usuario> findByRol(Rol rol);

        @Query("SELECT u FROM Usuario u WHERE u.rol = :rol")
    Page<Usuario> paginarPorRol(Rol rol, Pageable pageable);
    @Query("SELECT u FROM Usuario u WHERE u.rol IN :roles")
    Page<Usuario> paginarPorRolesIn(List<Rol> roles, Pageable pageable);
    @Query("SELECT u FROM Usuario u WHERE u.rol = :rol AND u.organizacion IS NULL")
    List<Usuario> findByRolAndOrganizacionIsNull(Rol rol);
}
