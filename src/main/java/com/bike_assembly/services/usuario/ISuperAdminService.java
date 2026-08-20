package com.bike_assembly.services.usuario;

import com.bike_assembly.domain.Usuario;
import java.util.List;

public interface ISuperAdminService {
    List<Usuario> listarAdministradores();
    List<Usuario> listarClientes();
    
    void promoverAAdmin(Long idUsuario, String nombreTaller , String direccionTaller, String telefonoTaller );

}