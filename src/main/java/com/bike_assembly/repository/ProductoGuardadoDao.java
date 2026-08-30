package com.bike_assembly.repository;

import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.ProductoGuardado;
import com.bike_assembly.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductoGuardadoDao extends JpaRepository<ProductoGuardado, Long> {

    List<ProductoGuardado> findByUsuario(Usuario usuario);

    Optional<ProductoGuardado> findByUsuarioAndProducto(Usuario usuario, Producto producto);

    boolean existsByUsuarioAndProducto(Usuario usuario, Producto producto);

    void deleteByUsuarioAndProducto(Usuario usuario, Producto producto);
}
