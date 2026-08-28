package com.bike_assembly.services.proveedor;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Proveedor;
import com.bike_assembly.repository.ProveedorDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
