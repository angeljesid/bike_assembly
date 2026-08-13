package com.bike_assembly.services.SuperAdmin;

import com.bike_assembly.domain.Usuario;
import com.bike_assembly.domain.Rol;
import java.util.List;

public interface ISuperAdminService {
    Usuario crearAdminConOrganizacion(String nombreOrganizacion, Usuario nuevoAdmin);
    List<Usuario> listarTodosLosUsuarios();
    void cambiarRolUsuario(Long idUsuario, Rol nuevoRol);
}