package com.bike_assembly.repository;

import com.bike_assembly.domain.OrdenServicio;
import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenServicioDao extends JpaRepository<OrdenServicio, Long> {

    List<OrdenServicio> findByOrganizacionAndEstado(Organizacion organizacion, String estado);

    List<OrdenServicio> findByOrganizacionAndEstadoNotIn(Organizacion organizacion, List<String> estados);

    List<OrdenServicio> findByOperarioAsignadoAndEstado(Usuario operario, String estado);

    List<OrdenServicio> findByOperarioAsignadoAndEstadoNotIn(Usuario operario, List<String> estados);

    List<OrdenServicio> findByOperarioAsignado(Usuario operario);

    List<OrdenServicio> findByClienteUsuarioAndOrganizacion(Usuario cliente, Organizacion organizacion);

    List<OrdenServicio> findByOrganizacion(Organizacion organizacion);
}
