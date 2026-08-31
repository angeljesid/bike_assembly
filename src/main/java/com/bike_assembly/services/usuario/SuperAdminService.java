package com.bike_assembly.services.usuario;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.OrganizacionDao;
import com.bike_assembly.repository.UsuarioDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SuperAdminService implements ISuperAdminService {

    private static final String MOTIVO_AUTOMATICO = "Bloqueo Automático por Vencimiento";
    private static final String MOTIVO_MANUAL = "Bloqueo Manual por SuperAdmin";

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private OrganizacionDao organizacionDao;

    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> listarAdministradores(Pageable pageable) {
        return usuarioDao.paginarPorRol(Rol.ADMIN, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> listarClientes(Pageable pageable) {
        return usuarioDao.paginarPorRolSinPlan(Rol.CLIENTE, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> listarEmpleados(Pageable pageable) {
        return usuarioDao.paginarPorRolesIn(List.of(Rol.OPERARIO, Rol.CAJA), pageable);
    }

    @Override
    @Transactional
    public void promoverAAdmin(Long idUsuario, String nombreTaller, String direccionTaller, String telefonoTaller, String tipo, BigDecimal precioPlan, LocalDate fechaVencimiento) {
        Usuario usuario = usuarioDao.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (precioPlan == null || fechaVencimiento == null) {
            throw new IllegalArgumentException("Debes ingresar el precio del plan y la fecha de vencimiento.");
        }

        Organizacion nuevaOrg = new Organizacion();
        nuevaOrg.setNombre(nombreTaller);
        // Se asocian los valores reales enviados desde el modal HTML
        nuevaOrg.setDireccion(direccionTaller);
        nuevaOrg.setTelefono(telefonoTaller);
        nuevaOrg.setTipo(tipo);
        nuevaOrg.setFechaVencimiento(fechaVencimiento);
        nuevaOrg.setPrecioPlan(precioPlan);
        nuevaOrg.setActivo(true);
        Organizacion orgGuardada = organizacionDao.save(nuevaOrg);

        usuario.setRol(Rol.ADMIN);
        usuario.setOrganizacion(orgGuardada);
        usuario.setActivo(true);
        usuario.setPlanSolicitado(null);
        usuario.setPlanEstado(null);
        usuario.setPrecioPlanSolicitado(null);
        usuario.setFechaSolicitudPlan(null);
        if (usuario.getFechaCreacion() == null) {
            usuario.setFechaCreacion(LocalDateTime.now());
        }
        usuarioDao.save(usuario);
    }

    @Override
    @Transactional
    public void actualizarAdminYOrganizacion(Long idUsuario, String nombreTaller, String direccion, String telefono, String tipo, BigDecimal precioPlan, LocalDate fechaVencimiento) {
        Usuario usuario = usuarioDao.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getOrganizacion() != null) {
            Organizacion org = usuario.getOrganizacion();
            org.setNombre(nombreTaller);
            org.setDireccion(direccion);
            org.setTelefono(telefono);
            org.setTipo(tipo);
            org.setPrecioPlan(precioPlan);
            org.setFechaVencimiento(fechaVencimiento);

            if (fechaVencimiento != null && fechaVencimiento.isAfter(LocalDate.now())) {
                org.setActivo(true);
                org.setMotivoSuspension(null);
                org.setFechaSuspension(null);
                List<Usuario> vinculados = usuarioDao.findByOrganizacion(org);
                for (Usuario u : vinculados) {
                    u.setActivo(true);
                }
                usuarioDao.saveAll(vinculados);
            }

            organizacionDao.save(org);
        }

        if (usuario.getPlanSolicitado() != null) {
            usuario.setPlanSolicitado(null);
            usuario.setPlanEstado(null);
            usuario.setPrecioPlanSolicitado(null);
            usuario.setFechaSolicitudPlan(null);
            usuarioDao.save(usuario);
        }
    }

    @Override
    @Transactional
    public void cambiarEstadoTaller(Long idOrganizacion, boolean activo, String motivoManual) {
        Organizacion org = organizacionDao.findById(idOrganizacion)
                .orElseThrow(() -> new RuntimeException("Organización no encontrada"));

        boolean vencida = org.getFechaVencimiento() != null && org.getFechaVencimiento().isBefore(LocalDate.now());
        boolean estadoFinal = activo && !vencida;

        org.setActivo(estadoFinal);

        if (!estadoFinal) {
            org.setMotivoSuspension(motivoManual != null && !motivoManual.isBlank()
                    ? "Bloqueo Manual: " + motivoManual
                    : MOTIVO_MANUAL);
            org.setFechaSuspension(LocalDateTime.now());
        } else {
            org.setMotivoSuspension(null);
            org.setFechaSuspension(null);
        }

        organizacionDao.save(org);

        List<Usuario> usuariosVinculados = usuarioDao.findByOrganizacion(org);
        for (Usuario u : usuariosVinculados) {
            u.setActivo(estadoFinal);
        }
        usuarioDao.saveAll(usuariosVinculados);
    }

    @Override
    @Transactional
    public void cambiarEstadoUsuario(Long idUsuario, boolean activo) {
        Usuario usuario = usuarioDao.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setActivo(activo);
        usuarioDao.save(usuario);
    }

    @Override
    @Transactional
    public void actualizarDatosBasicosUsuario(Long idUsuario, String nombre, String cedula, String email) {
        Usuario usuario = usuarioDao.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setNombre(nombre);
        usuario.setCedula(cedula);
        usuario.setEmail(email);
        usuarioDao.save(usuario);
    }

    @Override
    @Transactional
    public void actualizarEmpleado(Long idUsuario, String nombre, String cedula, String email) {
        Usuario usuario = usuarioDao.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        usuario.setNombre(nombre);
        usuario.setCedula(cedula);
        usuario.setEmail(email);
        usuarioDao.save(usuario);
    }

    @Override
    @Transactional
    public void sincronizarTalleresVencidos() {
        LocalDate hoy = LocalDate.now();
        List<Organizacion> organizaciones = organizacionDao.findAll();
        for (Organizacion org : organizaciones) {
            if (org.getFechaVencimiento() != null && org.getFechaVencimiento().isBefore(hoy)
                    && (org.getActivo() == null || org.getActivo())) {
                org.setActivo(false);
                org.setMotivoSuspension(MOTIVO_AUTOMATICO);
                org.setFechaSuspension(LocalDateTime.now());
                organizacionDao.save(org);

                List<Usuario> vinculados = usuarioDao.findByOrganizacion(org);
                for (Usuario u : vinculados) {
                    u.setActivo(false);
                }
                usuarioDao.saveAll(vinculados);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Organizacion> listarOrganizaciones() {
        return organizacionDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Organizacion> listarOrganizacionesPorVencer(int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(dias);
        List<Organizacion> organizaciones = organizacionDao.findAll();
        List<Organizacion> resultado = new ArrayList<>();
        for (Organizacion org : organizaciones) {
            boolean activa = org.getActivo() == null || org.getActivo();
            if (activa && org.getFechaVencimiento() != null
                    && !org.getFechaVencimiento().isBefore(hoy)
                    && !org.getFechaVencimiento().isAfter(limite)) {
                resultado.add(org);
            }
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Organizacion> listarOrganizacionesSuspendidas() {
        List<Organizacion> organizaciones = organizacionDao.findAll();
        List<Organizacion> resultado = new ArrayList<>();
        for (Organizacion org : organizaciones) {
            if (org.getActivo() != null && !org.getActivo()) {
                resultado.add(org);
            }
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> contarTalleresPorTipo() {
        Map<String, Long> conteo = new LinkedHashMap<>();
        List<Organizacion> organizaciones = organizacionDao.findAll();
        for (Organizacion org : organizaciones) {
            if (org.getActivo() == null || org.getActivo()) {
                String tipo = org.getTipo() == null || org.getTipo().isBlank() ? "Sin Plan" : org.getTipo();
                conteo.put(tipo, conteo.getOrDefault(tipo, 0L) + 1);
            }
        }
        return conteo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Organizacion> listarTalleresVencidos() {
        LocalDate hoy = LocalDate.now();
        List<Organizacion> organizaciones = organizacionDao.findAll();
        List<Organizacion> resultado = new ArrayList<>();
        for (Organizacion org : organizaciones) {
            if (org.getFechaVencimiento() != null && org.getFechaVencimiento().isBefore(hoy)) {
                resultado.add(org);
            }
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerIngresosActivos() {
        return organizacionDao.sumarPrecioPlanesActivos();
    }

    @Override
    @Transactional
    public void asignarTallerYPlan(Long idUsuario, String nombreTaller, String direccion, String telefono,
                                   String tipo, BigDecimal precioPlan, LocalDate fechaVencimiento) {
        Usuario usuario = usuarioDao.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getRol() != Rol.ADMIN || usuario.getOrganizacion() != null) {
            throw new RuntimeException("La cuenta ya tiene un taller asignado o no corresponde a un administrador huérfano.");
        }

        if (precioPlan == null || fechaVencimiento == null) {
            throw new IllegalArgumentException("Debes ingresar el precio del plan y la fecha de vencimiento.");
        }

        Organizacion org = new Organizacion();
        org.setNombre(nombreTaller);
        org.setDireccion(direccion);
        org.setTelefono(telefono);
        org.setTipo(tipo);
        org.setPrecioPlan(precioPlan);
        org.setFechaVencimiento(fechaVencimiento);
        org.setActivo(true);
        Organizacion guardada = organizacionDao.save(org);

        usuario.setOrganizacion(guardada);
        usuario.setActivo(true);
        if (usuario.getFechaCreacion() == null) {
            usuario.setFechaCreacion(LocalDateTime.now());
        }
        usuarioDao.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarAdministradoresHuerfanos() {
        return usuarioDao.findByRolAndOrganizacionIsNull(Rol.ADMIN);
    }

    @Override
    @Transactional
    public int purgarCuentasHuerfanas(int dias) {
        LocalDateTime limite = LocalDateTime.now().minusDays(dias);
        List<Usuario> huerfanos = usuarioDao.findByRolAndOrganizacionIsNull(Rol.ADMIN);
        List<Usuario> aEliminar = new ArrayList<>();
        for (Usuario u : huerfanos) {
            LocalDateTime creado = u.getFechaCreacion();
            if (creado != null && creado.isBefore(limite)) {
                aEliminar.add(u);
            }
        }
        if (!aEliminar.isEmpty()) {
            usuarioDao.deleteAll(aEliminar);
        }
        return aEliminar.size();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> listarSolicitudesPlanNuevas(Pageable pageable) {
        return usuarioDao.findByRolAndPlanSolicitadoIsNotNull(Rol.CLIENTE, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> listarSolicitudesCambioPlan(Pageable pageable) {
        return usuarioDao.findByRolAndPlanSolicitadoIsNotNull(Rol.ADMIN, pageable);
    }

    @Override
    @Transactional
    public void rechazarSolicitudPlan(Long idUsuario) {
        Usuario usuario = usuarioDao.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        limpiarSolicitudPlan(usuario);
        usuarioDao.save(usuario);
    }

    private void limpiarSolicitudPlan(Usuario usuario) {
        usuario.setPlanSolicitado(null);
        usuario.setPlanEstado(null);
        usuario.setPrecioPlanSolicitado(null);
        usuario.setFechaSolicitudPlan(null);
    }
}
