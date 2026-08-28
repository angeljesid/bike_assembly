package com.bike_assembly.services.empleado;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.UsuarioDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmpleadoServiceImpl implements IEmpleadoService {

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarEmpleadosPorOrganizacion(Organizacion organizacion) {
        return usuarioDao.findByOrganizacionAndActivoTrue(organizacion);
    }

    @Override
    @Transactional
    public Usuario guardarEmpleado(Usuario admin, Usuario nuevoEmpleado, Rol rol) {
        Organizacion org = admin.getOrganizacion();
        String tipo = org.getTipo();

        // Consultas soportadas por tu UsuarioDao actual
        long countOperarios = usuarioDao.countByOrganizacionAndRolAndActivoTrue(org, Rol.OPERARIO);
        long countCajas = usuarioDao.countByOrganizacionAndRolAndActivoTrue(org, Rol.CAJA);

        validarLimites(tipo, rol, countOperarios, countCajas);

        nuevoEmpleado.setRol(rol);
        nuevoEmpleado.setOrganizacion(org);
        nuevoEmpleado.setActivo(true);
        nuevoEmpleado.setPassword(passwordEncoder.encode(nuevoEmpleado.getPassword()));

        return usuarioDao.save(nuevoEmpleado);
    }

    @Override
    @Transactional
    public void eliminarEmpleado(Long idUsuario, Organizacion orgAdmin) {
        Usuario empleado = usuarioDao.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        if (empleado.getOrganizacion() == null || !empleado.getOrganizacion().getIdOrganizacion().equals(orgAdmin.getIdOrganizacion())) {
            throw new RuntimeException("Acceso denegado a esta organización");
        }

        // Eliminación lógica
        empleado.setActivo(false);
        usuarioDao.save(empleado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarEmpleadosSuspendidosPorOrganizacion(Organizacion organizacion) {
        return usuarioDao.findByOrganizacion(organizacion).stream()
                .filter(u -> u.getRol() == Rol.OPERARIO || u.getRol() == Rol.CAJA)
                .filter(u -> !Boolean.TRUE.equals(u.getActivo()))
                .toList();
    }

    @Override
    @Transactional
    public void suspenderEmpleado(Long idUsuario, Organizacion orgAdmin) {
        Usuario empleado = usuarioDao.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        if (empleado.getOrganizacion() == null || !empleado.getOrganizacion().getIdOrganizacion().equals(orgAdmin.getIdOrganizacion())) {
            throw new RuntimeException("Acceso denegado a esta organización");
        }

        if (empleado.getRol() == null || (empleado.getRol() != Rol.OPERARIO && empleado.getRol() != Rol.CAJA)) {
            throw new RuntimeException("No puedes suspender esta cuenta");
        }

        empleado.setActivo(false);
        usuarioDao.save(empleado);
    }

    @Override
    @Transactional
    public boolean activarEmpleado(Long idUsuario, Organizacion orgAdmin) {
        Usuario empleado = usuarioDao.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        if (empleado.getOrganizacion() == null || !empleado.getOrganizacion().getIdOrganizacion().equals(orgAdmin.getIdOrganizacion())) {
            throw new RuntimeException("Acceso denegado a esta organización");
        }

        if (empleado.getRol() == null || (empleado.getRol() != Rol.OPERARIO && empleado.getRol() != Rol.CAJA)) {
            throw new RuntimeException("No puedes activar esta cuenta");
        }

        long operariosActivos = usuarioDao.countByOrganizacionAndRolAndActivoTrue(orgAdmin, Rol.OPERARIO);
        long cajerosActivos = usuarioDao.countByOrganizacionAndRolAndActivoTrue(orgAdmin, Rol.CAJA);
        long totalActivos = operariosActivos + cajerosActivos;

        int limite = obtenerLimiteTotal(orgAdmin.getTipo());

        if (totalActivos >= limite) {
            return false;
        }

        empleado.setActivo(true);
        usuarioDao.save(empleado);
        return true;
    }

    private int obtenerLimiteTotal(String tipo) {
        if ("Taller Pequeño".equalsIgnoreCase(tipo)) {
            return 4;
        } else if ("Taller Mediano".equalsIgnoreCase(tipo)) {
            return 6;
        } else if ("Taller Grande".equalsIgnoreCase(tipo)) {
            return 12;
        }
        return 4;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario encontrarEmpleado(Long id) {
        return usuarioDao.findById(id).orElse(null);
    }

    private void validarLimites(String tipo, Rol rolNuevo, long actualesOperarios, long actualesCajas) {
        int maxOperarios = 0;
        int maxCajas = 0;

        if ("Taller Pequeño".equalsIgnoreCase(tipo)) {
            maxOperarios = 3;
            maxCajas = 1;
        } else if ("Taller Mediano".equalsIgnoreCase(tipo)) {
            maxOperarios = 6;
            maxCajas = 2;
        } else if ("Taller Grande".equalsIgnoreCase(tipo)) {
            maxOperarios = 10;
            maxCajas = 3;
        }

        if (rolNuevo == Rol.OPERARIO && actualesOperarios >= maxOperarios) {
            throw new RuntimeException("Límite de Operarios alcanzado para su plan actual.");
        }
        if (rolNuevo == Rol.CAJA && actualesCajas >= maxCajas) {
            throw new RuntimeException("Límite de Cajeros alcanzado para su plan actual.");
        }
    }
}