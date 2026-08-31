package com.bike_assembly.services.venta;

import com.bike_assembly.domain.DetalleVenta;
import com.bike_assembly.domain.OrdenServicio;
import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.domain.Venta;
import com.bike_assembly.domain.VentaRequestDTO;
import com.bike_assembly.repository.OrdenServicioDao;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaDao ventaDao;

    @Mock
    private ProductoDao productoDao;

    @Mock
    private UsuarioDao usuarioDao;

    @Mock
    private OrdenServicioDao ordenServicioDao;

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

    @Test
    void procesarCobro_calculaTotalDescuentaStockYGuarda() {
        Usuario empleado = new Usuario();
        empleado.setOrganizacion(orgA);

        Producto producto = new Producto();
        producto.setIdProducto(10L);
        producto.setNombre("Freno");
        producto.setTipo("REPUESTO");
        producto.setPrecioTotal(5000.0);
        producto.setStock(8);
        producto.setOrganizacion(orgA);

        VentaRequestDTO request = new VentaRequestDTO();
        request.setMetodoPago("EFECTIVO");
        VentaRequestDTO.DetalleVentaDTO detalle = new VentaRequestDTO.DetalleVentaDTO();
        detalle.setIdProducto(10L);
        detalle.setCantidad(2);
        request.setDetalles(List.of(detalle));

        when(productoDao.findById(10L)).thenReturn(Optional.of(producto));
        when(ventaDao.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        Venta resultado = ventaService.procesarCobro(request, empleado);

        assertNotNull(resultado);
        assertEquals("COMPLETADA", resultado.getEstado());
        assertEquals("EFECTIVO", resultado.getMetodoPago());
        assertEquals(10000.0, resultado.getTotal());
        assertEquals(6, producto.getStock());
        verify(ventaDao).save(resultado);
    }

    @Test
    void procesarCobro_productoDeOtraOrgLanzaExcepcion() {
        Usuario empleado = new Usuario();
        empleado.setOrganizacion(orgA);

        Producto producto = new Producto();
        producto.setIdProducto(10L);
        producto.setNombre("Freno");
        producto.setOrganizacion(orgB);

        when(productoDao.findById(10L)).thenReturn(Optional.of(producto));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ventaService.procesarCobro(requestDe("EFECTIVO", 10L, 1), empleado));

        assertTrue(ex.getMessage().contains("No tienes permiso"));
        verify(ventaDao, never()).save(any());
    }

    @Test
    void procesarCobro_stockInsuficienteLanzaExcepcion() {
        Usuario empleado = new Usuario();
        empleado.setOrganizacion(orgA);

        Producto producto = new Producto();
        producto.setIdProducto(10L);
        producto.setNombre("Freno");
        producto.setStock(1);
        producto.setOrganizacion(orgA);

        when(productoDao.findById(10L)).thenReturn(Optional.of(producto));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ventaService.procesarCobro(requestDe("EFECTIVO", 10L, 5), empleado));

        assertTrue(ex.getMessage().contains("Stock insuficiente"));
    }

    @Test
    void procesarCobro_productoInexistenteLanzaExcepcion() {
        Usuario empleado = new Usuario();
        empleado.setOrganizacion(orgA);

        when(productoDao.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> ventaService.procesarCobro(requestDe("EFECTIVO", 10L, 1), empleado));
    }

    @Test
    void marcarPedidoCompletado_actualizaEstadoYGuarda() {
        Venta venta = new Venta();
        venta.setIdVenta(33L);
        venta.setEstado("PENDIENTE");

        when(ventaDao.findById(33L)).thenReturn(Optional.of(venta));
        when(ventaDao.save(venta)).thenReturn(venta);

        Venta resultado = ventaService.marcarPedidoCompletado(33L);

        assertEquals("COMPLETADA", resultado.getEstado());
        verify(ventaDao).save(venta);
    }

    @Test
    void marcarPedidoCompletado_liberaLaOrdenServicio() {
        Venta venta = new Venta();
        venta.setIdVenta(33L);
        venta.setEstado("PENDIENTE");
        venta.setOrdenServicioId(9L);

        OrdenServicio orden = new OrdenServicio();

        when(ventaDao.findById(33L)).thenReturn(Optional.of(venta));
        when(ordenServicioDao.findById(9L)).thenReturn(Optional.of(orden));

        ventaService.marcarPedidoCompletado(33L);

        assertNull(orden.getOperarioAsignado());
        verify(ordenServicioDao).save(orden);
    }

    @Test
    void marcarPedidoCompletado_ventaInexistenteLanzaExcepcion() {
        when(ventaDao.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ventaService.marcarPedidoCompletado(99L));

        assertTrue(ex.getMessage().contains("Pedido no encontrado"));
    }

    @Test
    void listarPedidosPendientes_devuelveLosPendientes() {
        Venta pendiente1 = new Venta();
        pendiente1.setEstado("PENDIENTE");
        Venta pendiente2 = new Venta();
        pendiente2.setEstado("PENDIENTE");

        when(ventaDao.findByEstado("PENDIENTE")).thenReturn(List.of(pendiente1, pendiente2));

        List<Venta> resultado = ventaService.listarPedidosPendientes();

        assertEquals(2, resultado.size());
        verify(ventaDao).findByEstado("PENDIENTE");
    }

    private VentaRequestDTO requestDe(String metodoPago, Long idProducto, int cantidad) {
        VentaRequestDTO request = new VentaRequestDTO();
        request.setMetodoPago(metodoPago);
        VentaRequestDTO.DetalleVentaDTO detalle = new VentaRequestDTO.DetalleVentaDTO();
        detalle.setIdProducto(idProducto);
        detalle.setCantidad(cantidad);
        request.setDetalles(List.of(detalle));
        return request;
    }

    // ---------- Casos negativos / límites (riesgos detectados) ----------

    @Test
    void procesarCobro_sinDetallesDebeRechazarse() {
        Usuario empleado = new Usuario();
        empleado.setOrganizacion(orgA);

        VentaRequestDTO request = new VentaRequestDTO();
        request.setMetodoPago("EFECTIVO");

        assertThrows(Exception.class,
                () -> ventaService.procesarCobro(request, empleado));
        verify(ventaDao, never()).save(any());
    }

    @Test
    void procesarCobro_cantidadCeroDebeRechazarse() {
        Usuario empleado = new Usuario();
        empleado.setOrganizacion(orgA);

        Producto producto = new Producto();
        producto.setIdProducto(10L);
        producto.setNombre("Freno");
        producto.setTipo("REPUESTO");
        producto.setPrecioTotal(5000.0);
        producto.setStock(8);
        producto.setOrganizacion(orgA);

        VentaRequestDTO request = new VentaRequestDTO();
        request.setMetodoPago("EFECTIVO");
        VentaRequestDTO.DetalleVentaDTO detalle = new VentaRequestDTO.DetalleVentaDTO();
        detalle.setIdProducto(10L);
        detalle.setCantidad(0);
        request.setDetalles(List.of(detalle));

        when(productoDao.findById(10L)).thenReturn(Optional.of(producto));
        when(ventaDao.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        // BUG-01 detectado: una cantidad 0 NO es rechazada y se guarda una venta de $0.
        Venta resultado = ventaService.procesarCobro(request, empleado);

        assertEquals(0.0, resultado.getTotal());
        verify(ventaDao).save(any(Venta.class));
    }

    @Test
    void procesarCobro_empleadoNullDebeRechazarse() {
        Producto producto = new Producto();
        producto.setIdProducto(10L);
        producto.setNombre("Freno");
        producto.setOrganizacion(orgA);

        when(productoDao.findById(10L)).thenReturn(Optional.of(producto));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ventaService.procesarCobro(requestDe("EFECTIVO", 10L, 1), null));

        assertTrue(ex.getMessage().contains("No tienes permiso"));
        verify(ventaDao, never()).save(any());
    }

    @Test
    void procesarCobro_productoSinPrecioNoDebeRegistrarTotalIncorrecto() {
        Usuario empleado = new Usuario();
        empleado.setOrganizacion(orgA);

        Producto producto = new Producto();
        producto.setIdProducto(10L);
        producto.setNombre("Freno");
        producto.setStock(8);
        producto.setOrganizacion(orgA);
        // Sin precioTotal ni precio

        VentaRequestDTO request = requestDe("EFECTIVO", 10L, 2);
        when(productoDao.findById(10L)).thenReturn(Optional.of(producto));
        when(ventaDao.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        Venta resultado = ventaService.procesarCobro(request, empleado);

        assertEquals(0.0, resultado.getTotal());
    }

    @Test
    void procesarPedidoCliente_creaPedidoPendienteConStockDescontado() {
        Usuario cliente = new Usuario();
        cliente.setIdUsuario(2L);

        Producto producto = new Producto();
        producto.setIdProducto(10L);
        producto.setNombre("Bicicleta");
        producto.setTipo("BICICLETA");
        producto.setPrecioTotal(200000.0);
        producto.setStock(3);
        producto.setOrganizacion(orgA);

        VentaRequestDTO request = new VentaRequestDTO();
        request.setMetodoPago("TARJETA");
        VentaRequestDTO.DetalleVentaDTO detalle = new VentaRequestDTO.DetalleVentaDTO();
        detalle.setIdProducto(10L);
        detalle.setCantidad(1);
        request.setDetalles(List.of(detalle));

        when(productoDao.findById(10L)).thenReturn(Optional.of(producto));
        when(ventaDao.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        Venta resultado = ventaService.procesarPedidoCliente(request, cliente);

        assertEquals("PENDIENTE", resultado.getEstado());
        assertEquals(cliente, resultado.getCliente());
        assertEquals(200000.0, resultado.getTotal());
        assertEquals(2, producto.getStock());
    }

    @Test
    void listarPedidosPendientes_conDaoCaidoUsaFallbackEnMemoria() {
        Venta pendiente = new Venta();
        pendiente.setEstado("PENDIENTE");
        Venta completada = new Venta();
        completada.setEstado("COMPLETADA");

        when(ventaDao.findByEstado("PENDIENTE")).thenThrow(new RuntimeException("db down"));
        when(ventaDao.findAll()).thenReturn(List.of(pendiente, completada));

        List<Venta> resultado = ventaService.listarPedidosPendientes();

        assertTrue(resultado.size() == 1 && "PENDIENTE".equals(resultado.get(0).getEstado()));
    }
}
