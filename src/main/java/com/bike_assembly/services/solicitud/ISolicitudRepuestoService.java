package com.bike_assembly.services.solicitud;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.SolicitudRepuesto;
import com.bike_assembly.domain.Usuario;

import java.util.List;

public interface ISolicitudRepuestoService {

    SolicitudRepuesto solicitar(Long idOrden, Producto producto, Integer cantidad, Usuario operario);

    List<SolicitudRepuesto> listarPendientes(Organizacion organizacion);

    boolean aceptar(Long idSolicitud, Usuario empleado);

    boolean rechazar(Long idSolicitud, Usuario empleado);

    List<SolicitudRepuesto> listarPorOrden(com.bike_assembly.domain.OrdenServicio orden);
}
