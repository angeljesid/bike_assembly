package com.bike_assembly.repository;

import com.bike_assembly.domain.OrdenServicio;
import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.SolicitudRepuesto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudRepuestoDao extends JpaRepository<SolicitudRepuesto, Long> {

    List<SolicitudRepuesto> findByOrganizacionAndEstado(Organizacion organizacion, String estado);

    List<SolicitudRepuesto> findByOrdenAndEstadoNotIn(OrdenServicio orden, List<String> estados);

    List<SolicitudRepuesto> findByOrden(OrdenServicio orden);
}
