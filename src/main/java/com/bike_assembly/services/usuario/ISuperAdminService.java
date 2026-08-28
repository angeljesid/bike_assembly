package com.bike_assembly.services.usuario;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ISuperAdminService {
    Page<Usuario> listarAdministradores(Pageable pageable);
    Page<Usuario> listarClientes(Pageable pageable);
    Page<Usuario> listarEmpleados(Pageable pageable);
    
    void promoverAAdmin(Long idUsuario, String nombreTaller, String direccionTaller, String telefonoTaller, String tipo, BigDecimal precioPlan, LocalDate fechaVencimiento);
    void actualizarAdminYOrganizacion(Long idUsuario, String nombreTaller, String direccion, String telefono, String tipo, BigDecimal precioPlan, LocalDate fechaVencimiento);
    void actualizarEmpleado(Long idUsuario, String nombre, String cedula, String email);
    List<Organizacion> listarOrganizaciones();
    List<Organizacion> listarOrganizacionesPorVencer(int dias);
    List<Organizacion> listarOrganizacionesSuspendidas();
    void sincronizarTalleresVencidos();
    Map<String, Long> contarTalleresPorTipo();
    List<Organizacion> listarTalleresVencidos();
    
    void cambiarEstadoTaller(Long idOrganizacion, boolean activo, String motivoManual);
    void cambiarEstadoUsuario(Long idUsuario, boolean activo);
    void actualizarDatosBasicosUsuario(Long idUsuario, String nombre, String cedula, String email);
    
    // Requerimientos adicionales
    BigDecimal obtenerIngresosActivos();
    void asignarTallerYPlan(Long idUsuario, String nombreTaller, String direccion, String telefono, String tipo, BigDecimal precioPlan, LocalDate fechaVencimiento);
    List<Usuario> listarAdministradoresHuerfanos();
    int purgarCuentasHuerfanas(int dias);
}
