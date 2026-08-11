package com.bike_assembly.services.SuperAdmin;

import com.bike_assembly.domain.Usuario;
import java.util.List;

public interface ISuperAdminService {
    Usuario crearAdminConOrganizacion(String nombreOrganizacion, Usuario nuevoAdmin);
    List<Usuario> listarAdministradores();
}