package com.bike_assembly.repository;

import com.bike_assembly.domain.Venta;
import com.bike_assembly.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface VentaDao extends JpaRepository<Venta, Long> {
    List<Venta> findByCliente(Usuario cliente);
}
