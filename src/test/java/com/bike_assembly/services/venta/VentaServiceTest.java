package com.bike_assembly.services.venta;

import com.bike_assembly.domain.DetalleVenta;
import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.domain.Venta;
import com.bike_assembly.repository.ProductoDao;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.repository.VentaDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaDao ventaDao;

    @Mock
    private ProductoDao productoDao;

    @Mock
    private UsuarioDao usuarioDao;

    @InjectMocks
    private VentaService ventaService;

    private Organizacion orgA;
    private Organizacion orgB;

    @BeforeEach
    void setUp() {
        orgA = new Organizacion("Taller A", "111", "Dir A");
        orgA.setIdOrganizacion(1L);
        orgB = new Organizacion("Taller B", "222", "Dir B");
        orgB.setIdOrganizacion(2L);
    }

    @Test
    void ventaConEmpleadoDeLaOrgPertenece() {
        Usuario empleado = new Usuario();
        empleado.setOrganizacion(orgA);

        Venta venta = new Venta();
        venta.setEmpleado(empleado);

        assertTrue(ventaService.ventaPerteneceAOrganizacion(venta, 1L));
    }

    @Test
    void ventaConProductoDeLaOrgPertenece() {
        Producto producto = new Producto();
        producto.setOrganizacion(orgA);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);

        Venta venta = new Venta();
        venta.getDetalles().add(detalle);

        assertTrue(ventaService.ventaPerteneceAOrganizacion(venta, 1L));
    }

    @Test
    void ventaAjenaNoPertenece() {
        Producto producto = new Producto();
        producto.setOrganizacion(orgB);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);

        Venta venta = new Venta();
        venta.getDetalles().add(detalle);

        assertFalse(ventaService.ventaPerteneceAOrganizacion(venta, 1L));
    }

    @Test
    void ventaNullNoPertenece() {
        assertFalse(ventaService.ventaPerteneceAOrganizacion(null, 1L));
    }

    @Test
    void listarVentasPorOrganizacionFiltraPorEstado() {
        Producto producto = new Producto();
        producto.setOrganizacion(orgA);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);

        Venta completada = new Venta();
        completada.setIdVenta(1L);
        completada.setEstado("COMPLETADA");
        completada.getDetalles().add(detalle);

        Venta pendiente = new Venta();
        pendiente.setIdVenta(2L);
        pendiente.setEstado("PENDIENTE");
        pendiente.getDetalles().add(detalle);

        when(ventaDao.findAll()).thenReturn(List.of(completada, pendiente));

        List<Venta> resultado = ventaService.listarVentasPorOrganizacion(1L, "PENDIENTE");

        assertTrue(resultado.size() == 1 && resultado.get(0).getIdVenta().equals(2L));
    }

    @Test
    void listarVentasPorOrganizacionSinEstadoTraeTodas() {
        Producto producto = new Producto();
        producto.setOrganizacion(orgA);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);

        Venta venta1 = new Venta();
        venta1.setIdVenta(1L);
        venta1.setEstado("COMPLETADA");
        venta1.getDetalles().add(detalle);

        Venta venta2 = new Venta();
        venta2.setIdVenta(2L);
        venta2.setEstado("PENDIENTE");
        venta2.getDetalles().add(detalle);

        when(ventaDao.findAll()).thenReturn(List.of(venta1, venta2));

        List<Venta> resultado = ventaService.listarVentasPorOrganizacion(1L, null);

        assertTrue(resultado.size() == 2);
    }
}
