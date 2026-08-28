package com.bike_assembly.services.usuario;

import com.bike_assembly.domain.Usuario;
import java.time.LocalDate;
import java.util.List;

public interface ISuperAdminService {
    List<Usuario> listarAdministradores();
    List<Usuario> listarClientes();
    List<Usuario> listarEmpleados();
    
    void promoverAAdmin(Long idUsuario, String nombreTaller, String direccionTaller, String telefonoTaller);
    void actualizarAdminYOrganizacion(Long idUsuario, String nombreTaller, String direccion, String telefono, String tipo, LocalDate fechaVencimiento);
    
    void cambiarEstadoTaller(Long idOrganizacion, boolean activo);
    void cambiarEstadoUsuario(Long idUsuario, boolean activo);
    void actualizarDatosBasicosUsuario(Long idUsuario, String nombre, String cedula, String email);
}