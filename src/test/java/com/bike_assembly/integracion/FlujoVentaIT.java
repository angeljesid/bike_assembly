package com.bike_assembly.integracion;

import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.domain.Venta;
import com.bike_assembly.domain.VentaRequestDTO;
import com.bike_assembly.repository.OrganizacionDao;
import com.bike_assembly.repository.ProductoDao;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.repository.VentaDao;
import com.bike_assembly.services.venta.VentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class FlujoVentaIT {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ProductoDao productoDao;

    @Autowired
    private OrganizacionDao organizacionDao;

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private VentaDao ventaDao;

    private Organizacion org;
    private Usuario cliente;

    @BeforeEach
    void setUp() {
        // Nombre unico por test (columna nombre es UNIQUE)
        org = organizacionDao.save(new Organizacion("Taller Flujo " + System.nanoTime(), "999", "Dir"));
        cliente = new Usuario();
        cliente.setUsername("cliente_flujo_" + System.nanoTime());
        cliente.setPassword("x");
        cliente.setEmail("cliente_flujo_" + System.nanoTime() + "@test.com");
        cliente.setRol(com.bike_assembly.domain.Rol.CLIENTE);
        cliente = usuarioDao.save(cliente);
    }

    @Test
    void PI11_pedidoValidoSePersisteEnLaBaseDeDatos() {
        Producto p = new Producto();
        p.setNombre("Bicicleta Montaña");
        p.setTipo("BICICLETA");
        p.setPrecioTotal(300000.0);
        p.setStock(5);
        p.setOrganizacion(org);
        p = productoDao.save(p);

        long antes = ventaDao.count();

        VentaRequestDTO request = new VentaRequestDTO();
        request.setMetodoPago("TARJETA");
        VentaRequestDTO.DetalleVentaDTO d = new VentaRequestDTO.DetalleVentaDTO();
        d.setIdProducto(p.getIdProducto());
        d.setCantidad(2);
        request.setDetalles(List.of(d));

        Venta venta = ventaService.procesarPedidoCliente(request, cliente);

        long despues = ventaDao.count();
        assertEquals(antes + 1, despues);
        assertTrue(venta.getIdVenta() != null);
        assertEquals("PENDIENTE", venta.getEstado());
    }

    @Test
    void PI12_pedidoInvalidoPorStockNoEscribeNadaEnLaBaseDeDatos() {
        Producto p = new Producto();
        p.setNombre("Llanta");
        p.setTipo("REPUESTO");
        p.setPrecioTotal(20000.0);
        p.setStock(1);
        p.setOrganizacion(org);
        p = productoDao.save(p);

        long antes = ventaDao.count();

        VentaRequestDTO request = new VentaRequestDTO();
        request.setMetodoPago("EFECTIVO");
        VentaRequestDTO.DetalleVentaDTO d = new VentaRequestDTO.DetalleVentaDTO();
        d.setIdProducto(p.getIdProducto());
        d.setCantidad(10); // supera stock
        request.setDetalles(List.of(d));

        assertThrows(RuntimeException.class,
                () -> ventaService.procesarPedidoCliente(request, cliente));

        long despues = ventaDao.count();
        assertEquals(antes, despues);
    }
}
