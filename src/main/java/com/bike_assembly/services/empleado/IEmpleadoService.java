package com.bike_assembly.services.empleado;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import java.util.List;

public interface IEmpleadoService {
    List<Usuario> listarEmpleadosPorOrganizacion(Organizacion organizacion);
    List<Usuario> listarEmpleadosSuspendidosPorOrganizacion(Organizacion organizacion);
    Usuario guardarEmpleado(Usuario admin, Usuario nuevoEmpleado, Rol rol);
    void eliminarEmpleado(Long idUsuario, Organizacion orgAdmin);
    void suspenderEmpleado(Long idUsuario, Organizacion orgAdmin);
    boolean activarEmpleado(Long idUsuario, Organizacion orgAdmin);
    Usuario encontrarEmpleado(Long id);
}