package com.bike_assembly.services.usuario;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.OrganizacionDao;
import com.bike_assembly.repository.UsuarioDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SuperAdminService implements ISuperAdminService {

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private OrganizacionDao organizacionDao;

    @Override
    public List<Usuario> listarAdministradores() {
        return usuarioDao.findByRol(Rol.ADMIN);
    }

    @Override
    public List<Usuario> listarClientes() {
        return usuarioDao.findByRol(Rol.CLIENTE);
    }

    @Override
    public List<Usuario> listarEmpleados() {
        List<Usuario> empleados = new ArrayList<>();
        empleados.addAll(usuarioDao.findByRol(Rol.OPERARIO));
        empleados.addAll(usuarioDao.findByRol(Rol.CAJA));
        return empleados;
    }

    @Override
    @Transactional
    public void promoverAAdmin(Long idUsuario, String nombreTaller, String direccionTaller, String telefonoTaller, String tipo, LocalDate fechaVencimiento) {
        Usuario usuario = usuarioDao.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Organizacion nuevaOrg = new Organizacion();
        nuevaOrg.setNombre(nombreTaller);
        // Se asocian los valores reales enviados desde el modal HTML
        nuevaOrg.setDireccion(direccionTaller);
        nuevaOrg.setTelefono(telefonoTaller);
        nuevaOrg.setTipo(tipo);
        nuevaOrg.setFechaVencimiento(fechaVencimiento);
        nuevaOrg.setActivo(true);
        Organizacion orgGuardada = organizacionDao.save(nuevaOrg);

        usuario.setRol(Rol.ADMIN);
        usuario.setOrganizacion(orgGuardada);
        usuarioDao.save(usuario);
    }

    @Override
    @Transactional
    public void actualizarAdminYOrganizacion(Long idUsuario, String nombreTaller, String direccion, String telefono, String tipo, LocalDate fechaVencimiento) {
        Usuario usuario = usuarioDao.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getOrganizacion() != null) {
            Organizacion org = usuario.getOrganizacion();
            org.setNombre(nombreTaller);
            org.setDireccion(direccion);
            org.setTelefono(telefono);
            org.setTipo(tipo);
            org.setFechaVencimiento(fechaVencimiento);
            
            if (fechaVencimiento != null && fechaVencimiento.isAfter(LocalDate.now())) {
                org.setActivo(true);
                List<Usuario> vinculados = usuarioDao.findByOrganizacion(org);
                for(Usuario u : vinculados) {
                    u.setActivo(true);
                }
                usuarioDao.saveAll(vinculados);
            }
            
            organizacionDao.save(org);
        }
    }

    @Override
    @Transactional
    public void cambiarEstadoTaller(Long idOrganizacion, boolean activo) {
        Organizacion org = organizacionDao.findById(idOrganizacion)
                .orElseThrow(() -> new RuntimeException("Organización no encontrada"));

        boolean vencida = org.getFechaVencimiento() != null && org.getFechaVencimiento().isBefore(LocalDate.now());
        boolean estadoFinal = activo && !vencida;

        org.setActivo(estadoFinal);
        organizacionDao.save(org);

        List<Usuario> usuariosVinculados = usuarioDao.findByOrganizacion(org);
        for(Usuario u : usuariosVinculados) {
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
    public List<Organizacion> listarOrganizaciones() {
        return organizacionDao.findAll();
    }

    @Override
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
}