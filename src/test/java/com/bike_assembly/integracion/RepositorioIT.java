package com.bike_assembly.integracion;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.Proveedor;
import com.bike_assembly.domain.Rol;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.OrganizacionDao;
import com.bike_assembly.repository.ProductoDao;
import com.bike_assembly.repository.ProveedorDao;
import com.bike_assembly.repository.UsuarioDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class RepositorioIT {

    @Autowired
    private ProductoDao productoDao;

    @Autowired
    private ProveedorDao proveedorDao;

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private OrganizacionDao organizacionDao;

    @Test
    void PI06_guardarProductoAsignaIdYSeRecupera() {
        Organizacion org = new Organizacion("Taller IT", "111", "Dir");
        org = organizacionDao.save(org);

        Producto p = new Producto();
        p.setNombre("Freno Hidráulico");
        p.setTipo("REPUESTO");
        p.setPrecioTotal(15000.0);
        p.setOrganizacion(org);

        Producto guardado = productoDao.save(p);
        assertNotNull(guardado.getIdProducto());

        Optional<Producto> recuperado = productoDao.findById(guardado.getIdProducto());
        assertTrue(recuperado.isPresent());
        assertEquals("Freno Hidráulico", recuperado.get().getNombre());
    }

    @Test
    void PI07_findByCodigoBarrasAndOrganizacionDevuelveElProducto() {
        Organizacion org = new Organizacion("Taller IT", "222", "Dir");
        org = organizacionDao.save(org);

        Producto p = new Producto();
        p.setNombre("Cadena");
        p.setTipo("REPUESTO");
        p.setCodigoBarras("CB-777");
        p.setOrganizacion(org);
        productoDao.save(p);

        Optional<Producto> resultado = productoDao.findByCodigoBarrasAndOrganizacion("CB-777", org);
        assertTrue(resultado.isPresent());
        assertEquals("CB-777", resultado.get().getCodigoBarras());
    }

    @Test
    void PI08_guardarProveedorConOrgYListarloPorOrg() {
        Organizacion org = new Organizacion("Taller IT", "333", "Dir");
        org = organizacionDao.save(org);

        Proveedor proveedor = new Proveedor();
        proveedor.setNombre("Distribuidora XYZ");
        proveedor.setOrganizacion(org);
        proveedorDao.save(proveedor);

        List<Proveedor> lista = proveedorDao.findByOrganizacion(org);
        assertEquals(1, lista.size());
        assertEquals("Distribuidora XYZ", lista.get(0).getNombre());
    }

    @Test
    void PI09_countByOrganizacionAndRolAndActivoTrueCuentaBien() {
        Organizacion org = new Organizacion("Taller IT", "444", "Dir");
        org = organizacionDao.save(org);

        Usuario operario1 = nuevoUsuario("op1", Rol.OPERARIO, org);
        Usuario operario2 = nuevoUsuario("op2", Rol.OPERARIO, org);
        Usuario cajero = nuevoUsuario("caja", Rol.CAJA, org);
        usuarioDao.save(operario1);
        usuarioDao.save(operario2);
        usuarioDao.save(cajero);

        long operarios = usuarioDao.countByOrganizacionAndRolAndActivoTrue(org, Rol.OPERARIO);
        long cajeros = usuarioDao.countByOrganizacionAndRolAndActivoTrue(org, Rol.CAJA);

        assertEquals(2, operarios);
        assertEquals(1, cajeros);
    }

    @Test
    void PI10_findByRolAndOrganizacionIsNullDevuelveSoloHuerfanos() {
        Organizacion org = new Organizacion("Taller IT", "555", "Dir");
        org = organizacionDao.save(org);

        Usuario huerfano = new Usuario();
        huerfano.setUsername("admin_huerfano");
        huerfano.setPassword("x");
        huerfano.setEmail("huerfano@test.com");
        huerfano.setRol(Rol.ADMIN);
        huerfano.setOrganizacion(null);
        usuarioDao.save(huerfano);

        Usuario conOrg = new Usuario();
        conOrg.setUsername("admin_org");
        conOrg.setPassword("x");
        conOrg.setEmail("conorg@test.com");
        conOrg.setRol(Rol.ADMIN);
        conOrg.setOrganizacion(org);
        usuarioDao.save(conOrg);

        List<Usuario> huerfanos = usuarioDao.findByRolAndOrganizacionIsNull(Rol.ADMIN);
        assertEquals(1, huerfanos.size());
        assertEquals("admin_huerfano", huerfanos.get(0).getUsername());
    }

    private Usuario nuevoUsuario(String username, Rol rol, Organizacion org) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword("x");
        u.setEmail(username + "@test.com");
        u.setRol(rol);
        u.setOrganizacion(org);
        return u;
    }
}
