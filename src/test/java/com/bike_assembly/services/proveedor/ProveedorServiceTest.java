package com.bike_assembly.services.proveedor;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Proveedor;
import com.bike_assembly.repository.ProveedorDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceTest {

    @Mock
    private ProveedorDao proveedorDao;

    @InjectMocks
    private ProveedorService proveedorService;

    @Test
    void proveedorDeLaOrgPertenece() {
        Organizacion org = new Organizacion("Taller", "111", "Dir");
        org.setIdOrganizacion(5L);

        Proveedor proveedor = new Proveedor();
        proveedor.setOrganizacion(org);

        assertTrue(proveedorService.proveedorPerteneceAOrganizacion(proveedor, 5L));
    }

    @Test
    void proveedorDeOtraOrgNoPertenece() {
        Organizacion org = new Organizacion("Taller", "111", "Dir");
        org.setIdOrganizacion(5L);

        Proveedor proveedor = new Proveedor();
        proveedor.setOrganizacion(org);

        assertFalse(proveedorService.proveedorPerteneceAOrganizacion(proveedor, 6L));
    }

    @Test
    void proveedorSinOrganizacionNoPertenece() {
        Proveedor proveedor = new Proveedor();
        assertFalse(proveedorService.proveedorPerteneceAOrganizacion(proveedor, 5L));
    }

    @Test
    void listarProveedoresDelegaEnElDao() {
        when(proveedorDao.findAll()).thenReturn(Arrays.asList(new Proveedor(), new Proveedor()));

        assertEquals(2, proveedorService.listarProveedores().size());
        verify(proveedorDao).findAll();
    }

    @Test
    void listarProveedoresPorOrganizacionDelegaEnElDao() {
        Organizacion org = new Organizacion("Taller", "111", "Dir");
        org.setIdOrganizacion(5L);

        when(proveedorDao.findByOrganizacion(org)).thenReturn(Arrays.asList(new Proveedor()));

        assertEquals(1, proveedorService.listarProveedoresPorOrganizacion(org).size());
        verify(proveedorDao).findByOrganizacion(org);
    }

    @Test
    void guardarProveedorDelegaEnElDao() {
        Proveedor proveedor = new Proveedor();

        proveedorService.guardar(proveedor);

        verify(proveedorDao).save(proveedor);
    }

    @Test
    void eliminarProveedorDelegaEnElDao() {
        Proveedor proveedor = new Proveedor();

        proveedorService.eliminar(proveedor);

        verify(proveedorDao).delete(proveedor);
    }

    @Test
    void encontrarProveedor_devuelveNullSiNoExiste() {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(9L);

        when(proveedorDao.findById(9L)).thenReturn(Optional.empty());

        assertNull(proveedorService.encontrarProveedor(proveedor));
    }
}
