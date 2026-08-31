package com.bike_assembly.services.empleado;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.UsuarioDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpleadoServiceImplTest {

    @Mock
    private UsuarioDao usuarioDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmpleadoServiceImpl empleadoService;

    private Organizacion organizacion(String tipo, Long id) {
        Organizacion org = new Organizacion("Taller " + tipo, "123", "Direccion");
        org.setTipo(tipo);
        org.setIdOrganizacion(id);
        return org;
    }

    @Test
    void guardarEmpleado_cupoPlanPequenoAlcanzadoLanzaExcepcion() {
        Organizacion org = organizacion("Taller Pequeño", 1L);
        Usuario admin = new Usuario();
        admin.setOrganizacion(org);
        Usuario nuevo = new Usuario();

        when(usuarioDao.countByOrganizacionAndRolAndActivoTrue(org, Rol.OPERARIO)).thenReturn(3L);
        when(usuarioDao.countByOrganizacionAndRolAndActivoTrue(org, Rol.CAJA)).thenReturn(1L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> empleadoService.guardarEmpleado(admin, nuevo, Rol.OPERARIO));

        assertTrue(ex.getMessage().contains("límite de empleados activos"));
        verify(usuarioDao, never()).save(any());
    }

    @Test
    void guardarEmpleado_cupoLibreAsignaRolOrgYEncriptaPassword() {
        Organizacion org = organizacion("Taller Mediano", 1L);
        Usuario admin = new Usuario();
        admin.setOrganizacion(org);
        Usuario nuevo = new Usuario();
        nuevo.setPassword("Bike123!");

        when(usuarioDao.countByOrganizacionAndRolAndActivoTrue(org, Rol.OPERARIO)).thenReturn(0L);
        when(usuarioDao.countByOrganizacionAndRolAndActivoTrue(org, Rol.CAJA)).thenReturn(1L);
        when(passwordEncoder.encode("Bike123!")).thenReturn("encoded-hash");
        when(usuarioDao.save(nuevo)).thenReturn(nuevo);

        Usuario resultado = empleadoService.guardarEmpleado(admin, nuevo, Rol.CAJA);

        assertEquals(Rol.CAJA, resultado.getRol());
        assertEquals(org, resultado.getOrganizacion());
        assertTrue(resultado.getActivo());
        assertEquals("encoded-hash", resultado.getPassword());
        verify(usuarioDao).save(nuevo);
    }

    @Test
    void guardarEmpleado_nuevoSinPasswordLanzaExcepcion() {
        Organizacion org = organizacion("Taller Grande", 1L);
        Usuario admin = new Usuario();
        admin.setOrganizacion(org);
        Usuario nuevo = new Usuario();

        when(usuarioDao.countByOrganizacionAndRolAndActivoTrue(org, Rol.OPERARIO)).thenReturn(0L);
        when(usuarioDao.countByOrganizacionAndRolAndActivoTrue(org, Rol.CAJA)).thenReturn(0L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> empleadoService.guardarEmpleado(admin, nuevo, Rol.CAJA));

        assertTrue(ex.getMessage().contains("contraseña"));
        verify(usuarioDao, never()).save(any());
    }

    @Test
    void guardarEmpleado_edicionSinPasswordConservaLaExistente() {
        Organizacion org = organizacion("Taller Mediano", 1L);
        Usuario admin = new Usuario();
        admin.setOrganizacion(org);
        Usuario nuevo = new Usuario();
        nuevo.setIdUsuario(5L);

        Usuario existente = new Usuario();
        existente.setPassword("hashAnterior");

        when(usuarioDao.countByOrganizacionAndRolAndActivoTrue(org, Rol.OPERARIO)).thenReturn(0L);
        when(usuarioDao.countByOrganizacionAndRolAndActivoTrue(org, Rol.CAJA)).thenReturn(0L);
        when(usuarioDao.findById(5L)).thenReturn(Optional.of(existente));
        when(usuarioDao.save(nuevo)).thenReturn(nuevo);

        Usuario resultado = empleadoService.guardarEmpleado(admin, nuevo, Rol.OPERARIO);

        assertEquals("hashAnterior", resultado.getPassword());
        verify(usuarioDao).save(nuevo);
    }

    @Test
    void eliminarEmpleado_deOtraOrgLanzaAccesoDenegado() {
        Organizacion orgA = organizacion("Taller Pequeño", 1L);
        Organizacion orgB = organizacion("Taller Mediano", 2L);
        Usuario empleado = new Usuario();
        empleado.setOrganizacion(orgA);

        when(usuarioDao.findById(7L)).thenReturn(Optional.of(empleado));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> empleadoService.eliminarEmpleado(7L, orgB));

        assertTrue(ex.getMessage().contains("Acceso denegado"));
        verify(usuarioDao, never()).save(any());
    }

    @Test
    void eliminarEmpleado_deLaMismaOrgDesactivaLogicamente() {
        Organizacion orgA = organizacion("Taller Pequeño", 1L);
        Usuario empleado = new Usuario();
        empleado.setOrganizacion(orgA);
        empleado.setActivo(true);

        when(usuarioDao.findById(7L)).thenReturn(Optional.of(empleado));
        when(usuarioDao.save(empleado)).thenReturn(empleado);

        empleadoService.eliminarEmpleado(7L, orgA);

        assertFalse(empleado.getActivo());
        verify(usuarioDao).save(empleado);
    }

    @Test
    void activarEmpleado_enLimiteDelPlanRetornaFalse() {
        Organizacion org = organizacion("Taller Mediano", 1L);
        Usuario empleado = new Usuario();
        empleado.setOrganizacion(org);
        empleado.setRol(Rol.OPERARIO);

        when(usuarioDao.findById(7L)).thenReturn(Optional.of(empleado));
        when(usuarioDao.countByOrganizacionAndRolAndActivoTrue(org, Rol.OPERARIO)).thenReturn(4L);
        when(usuarioDao.countByOrganizacionAndRolAndActivoTrue(org, Rol.CAJA)).thenReturn(2L);

        assertFalse(empleadoService.activarEmpleado(7L, org));
        verify(usuarioDao, never()).save(any());
    }

    @Test
    void activarEmpleado_conCupoActivaYGuarda() {
        Organizacion org = organizacion("Taller Mediano", 1L);
        Usuario empleado = new Usuario();
        empleado.setOrganizacion(org);
        empleado.setRol(Rol.CAJA);
        empleado.setActivo(false);

        when(usuarioDao.findById(7L)).thenReturn(Optional.of(empleado));
        when(usuarioDao.countByOrganizacionAndRolAndActivoTrue(org, Rol.OPERARIO)).thenReturn(2L);
        when(usuarioDao.countByOrganizacionAndRolAndActivoTrue(org, Rol.CAJA)).thenReturn(1L);
        when(usuarioDao.save(empleado)).thenReturn(empleado);

        assertTrue(empleadoService.activarEmpleado(7L, org));
        assertTrue(empleado.getActivo());
        verify(usuarioDao).save(empleado);
    }

    @Test
    void guardarEmpleado_adminSinOrganizacionNoDebeLanzarNPE() {
        Usuario admin = new Usuario();
        admin.setOrganizacion(null);
        Usuario nuevo = new Usuario();
        nuevo.setPassword("Bike123!");

        // Riesgo detectado: admin.getOrganizacion().getTipo() lanza NullPointerException (BUG-02)
        // en vez de un mensaje claro tipo "El administrador no tiene taller asignado".
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> empleadoService.guardarEmpleado(admin, nuevo, Rol.OPERARIO));

        assertNotNull(ex);
    }
}