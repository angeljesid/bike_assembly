package com.bike_assembly.services.usuario;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.OrganizacionDao;
import com.bike_assembly.repository.UsuarioDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuperAdminServiceTest {

    @Mock
    private UsuarioDao usuarioDao;

    @Mock
    private OrganizacionDao organizacionDao;

    @InjectMocks
    private SuperAdminService superAdminService;

    @Test
    void precioFijo_planPequenoVale50() {
        assertEquals(new BigDecimal("50"), ISuperAdminService.precioFijo("Taller Pequeño"));
    }

    @Test
    void precioFijo_planMedianoVale80() {
        assertEquals(new BigDecimal("80"), ISuperAdminService.precioFijo("Taller Mediano"));
    }

    @Test
    void precioFijo_planGrandeVale120() {
        assertEquals(new BigDecimal("120"), ISuperAdminService.precioFijo("Taller Grande"));
    }

    @Test
    void precioFijo_tipoInvalidoValeCero() {
        assertEquals(BigDecimal.ZERO, ISuperAdminService.precioFijo("Plan X"));
    }

    @Test
    void promoverAAdmin_creaOrganizacionConvierteAlUsuarioYGuarda() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(3L);
        usuario.setRol(Rol.CLIENTE);
        usuario.setPlanSolicitado("Taller Pequeño");

        when(usuarioDao.findById(3L)).thenReturn(Optional.of(usuario));
        when(organizacionDao.save(any(Organizacion.class))).thenAnswer(inv -> {
            Organizacion org = inv.getArgument(0);
            org.setIdOrganizacion(99L);
            return org;
        });

        superAdminService.promoverAAdmin(3L, "Taller Bici", "Cra 1 #2-3", "5551234",
                "Taller Pequeño", new BigDecimal("50"), LocalDate.of(2027, 1, 1));

        assertEquals(Rol.ADMIN, usuario.getRol());
        assertEquals(99L, usuario.getOrganizacion().getIdOrganizacion());
        assertEquals("Taller Pequeño", usuario.getOrganizacion().getTipo());
        assertTrue(usuario.getActivo());
        assertNull(usuario.getPlanSolicitado());
        assertNull(usuario.getPrecioPlanSolicitado());
        verify(usuarioDao).save(usuario);
    }

    @Test
    void promoverAAdmin_sinPrecioNiFechaLanzaIllegalArgument() {
        when(usuarioDao.findById(3L)).thenReturn(Optional.of(new Usuario()));

        assertThrows(IllegalArgumentException.class, () ->
                superAdminService.promoverAAdmin(3L, "Taller", "Dir", "555", "Taller Pequeño", null, null));
    }

    @Test
    void promoverAAdmin_usuarioInexistenteLanzaExcepcion() {
        when(usuarioDao.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                superAdminService.promoverAAdmin(99L, "Taller", "Dir", "555", "Taller Pequeño",
                        BigDecimal.ONE, LocalDate.now().plusDays(5)));

        assertTrue(ex.getMessage().contains("Usuario no encontrado"));
    }

    @Test
    void asignarTallerYPlan_adminHuerfanoAsignaOrgYPlanes() {
        Usuario admin = new Usuario();
        admin.setIdUsuario(3L);
        admin.setRol(Rol.ADMIN);

        when(usuarioDao.findById(3L)).thenReturn(Optional.of(admin));
        when(organizacionDao.save(any(Organizacion.class))).thenAnswer(inv -> {
            Organizacion org = inv.getArgument(0);
            org.setIdOrganizacion(77L);
            return org;
        });

        superAdminService.asignarTallerYPlan(3L, "Taller X", "Dir", "555",
                "Taller Grande", new BigDecimal("120"), LocalDate.of(2027, 2, 1));

        assertEquals(77L, admin.getOrganizacion().getIdOrganizacion());
        assertEquals("Taller Grande", admin.getOrganizacion().getTipo());
        assertEquals(new BigDecimal("120"), admin.getOrganizacion().getPrecioPlan());
        verify(usuarioDao).save(admin);
    }

    @Test
    void asignarTallerYPlan_cuentaConTallerLanzaExcepcion() {
        Usuario admin = new Usuario();
        admin.setIdUsuario(3L);
        admin.setRol(Rol.ADMIN);
        admin.setOrganizacion(new Organizacion("Taller Ya Asignado", "1", "Dir"));

        when(usuarioDao.findById(3L)).thenReturn(Optional.of(admin));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                superAdminService.asignarTallerYPlan(3L, "Taller X", "Dir", "555",
                        "Taller Grande", BigDecimal.ONE, LocalDate.now().plusDays(1)));

        assertTrue(ex.getMessage().contains("ya tiene un taller asignado"));
    }

    @Test
    void rechazarSolicitudPlan_limpiaLaSolicitudYGuarda() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(5L);
        usuario.setPlanSolicitado("Taller Pequeño");
        usuario.setPlanEstado("Solicitado");
        usuario.setPrecioPlanSolicitado(new BigDecimal("50"));
        usuario.setFechaSolicitudPlan(java.time.LocalDateTime.now());

        when(usuarioDao.findById(5L)).thenReturn(Optional.of(usuario));

        superAdminService.rechazarSolicitudPlan(5L);

        assertNull(usuario.getPlanSolicitado());
        assertNull(usuario.getPlanEstado());
        assertNull(usuario.getPrecioPlanSolicitado());
        assertNull(usuario.getFechaSolicitudPlan());
        verify(usuarioDao).save(usuario);
    }

    @Test
    void actualizarDatosBasicosUsuario_actualizaCamposYGuarda() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(4L);

        when(usuarioDao.findById(4L)).thenReturn(Optional.of(usuario));

        superAdminService.actualizarDatosBasicosUsuario(4L, "Ana", "123456", "ana@correo.com");

        assertEquals("Ana", usuario.getNombre());
        assertEquals("123456", usuario.getCedula());
        assertEquals("ana@correo.com", usuario.getEmail());
        verify(usuarioDao).save(usuario);
    }

    @Test
    void contarTalleresPorTipo_cuentaSoloTalleresActivos() {
        Organizacion pequena = new Organizacion("Taller A", "1", "Dir");
        pequena.setTipo("Taller Pequeño");
        pequena.setActivo(true);

        Organizacion mediana = new Organizacion("Taller B", "2", "Dir");
        mediana.setTipo("Taller Mediano");
        mediana.setActivo(false);

        when(organizacionDao.findAll()).thenReturn(List.of(pequena, mediana));

        Map<String, Long> conteo = superAdminService.contarTalleresPorTipo();

        assertEquals(1L, conteo.get("Taller Pequeño"));
        assertFalse(conteo.containsKey("Taller Mediano"));
    }

    // ---------- Métodos del SuperAdmin sin probar antes (riesgos) ----------

    @Test
    void sincronizarTalleresVencidos_suspendeOrgVencidaYUsuarios() {
        Organizacion vencida = new Organizacion("Taller Vencido", "1", "Dir");
        vencida.setFechaVencimiento(LocalDate.of(2020, 1, 1));
        vencida.setActivo(true);

        Organizacion vigente = new Organizacion("Taller Vigente", "2", "Dir");
        vigente.setFechaVencimiento(LocalDate.of(2030, 1, 1));
        vigente.setActivo(true);

        Usuario adminVencido = new Usuario();
        adminVencido.setActivo(true);

        when(organizacionDao.findAll()).thenReturn(List.of(vencida, vigente));
        when(usuarioDao.findByOrganizacion(vencida)).thenReturn(List.of(adminVencido));

        superAdminService.sincronizarTalleresVencidos();

        assertFalse(vencida.getActivo());
        assertEquals("Bloqueo Automático por Vencimiento", vencida.getMotivoSuspension());
        assertFalse(adminVencido.getActivo());
        assertTrue(vigente.getActivo());
    }

    @Test
    void cambiarEstadoTaller_orgVencidaNoSeReactiva() {
        Organizacion org = new Organizacion("Taller", "1", "Dir");
        org.setFechaVencimiento(LocalDate.of(2020, 1, 1));

        when(organizacionDao.findById(1L)).thenReturn(Optional.of(org));

        superAdminService.cambiarEstadoTaller(1L, true, null);

        assertFalse(org.getActivo());
        assertNotNull(org.getMotivoSuspension());
    }

    @Test
    void cambiarEstadoTaller_orgVigenteSeActivaYRegistraMotivo() {
        Organizacion org = new Organizacion("Taller", "1", "Dir");
        org.setFechaVencimiento(LocalDate.of(2030, 1, 1));
        org.setMotivoSuspension("Bloqueo Manual: X");

        when(organizacionDao.findById(1L)).thenReturn(Optional.of(org));

        superAdminService.cambiarEstadoTaller(1L, true, null);

        assertTrue(org.getActivo());
        assertNull(org.getMotivoSuspension());
    }

    @Test
    void purgarCuentasHuerfanas_eliminaSoloLasAntiguas() {
        Usuario antiguo = new Usuario();
        antiguo.setFechaCreacion(LocalDateTime.now().minusDays(10));
        Usuario reciente = new Usuario();
        reciente.setFechaCreacion(LocalDateTime.now().minusDays(1));

        when(usuarioDao.findByRolAndOrganizacionIsNull(Rol.ADMIN)).thenReturn(List.of(antiguo, reciente));

        int eliminados = superAdminService.purgarCuentasHuerfanas(5);

        assertEquals(1, eliminados);
        verify(usuarioDao).deleteAll(List.of(antiguo));
    }
}