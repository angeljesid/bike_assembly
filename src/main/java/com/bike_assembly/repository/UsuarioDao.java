package com.bike_assembly.repository;

import com.bike_assembly.domain.Usuario;
import com.bike_assembly.domain.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UsuarioDao extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByResetPasswordToken(String token);
    
    // El Pto Gmail :v
    Optional<Usuario> findByUsernameOrEmail(String username, String email);

    boolean existsByRol(Rol rol);
    List<Usuario> findByRol(Rol rol);
}