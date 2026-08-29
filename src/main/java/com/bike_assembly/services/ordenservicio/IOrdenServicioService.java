package com.bike_assembly.services.ordenservicio;

import com.bike_assembly.domain.OrdenServicio;
import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Usuario;

import java.util.List;

public interface IOrdenServicioService {

    OrdenServicio registrar(OrdenServicio orden, Usuario registrador);

    List<OrdenServicio> listarEnCola(Organizacion organizacion);

    List<OrdenServicio> listarPorOperario(Usuario operario);

    List<OrdenServicio> listarPorCliente(Usuario cliente);

    boolean tomarOrden(Long idOrden, Usuario operario);

    boolean actualizarEstado(Long idOrden, String nuevoEstado, Usuario operario);

    void facturarServicio(OrdenServicio orden, Usuario empleado);

    List<OrdenServicio> listarTodas(Organizacion organizacion);
}
