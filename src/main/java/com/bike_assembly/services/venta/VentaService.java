package com.bike_assembly.services.venta;

import com.bike_assembly.domain.DetalleVenta;
import com.bike_assembly.domain.OrdenServicio;
import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.domain.Venta;
import com.bike_assembly.domain.VentaRequestDTO;
import com.bike_assembly.repository.OrdenServicioDao;
import com.bike_assembly.repository.ProductoDao;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.repository.VentaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VentaService implements IVentaService {

    @Autowired
    private VentaDao ventaDao;

    @Autowired
    private ProductoDao productoDao;

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private OrdenServicioDao ordenServicioDao;
    
    @Override
    @Transactional
    public Venta procesarCobro(VentaRequestDTO request, Usuario empleado) {
        Venta venta = new Venta();
        venta.setFechaHora(LocalDateTime.now());
        venta.setMetodoPago(request.getMetodoPago());
        venta.setEstado("COMPLETADA");
        venta.setEmpleado(empleado);

        if (request.getIdCliente() != null) {
            venta.setCliente(usuarioDao.findById(request.getIdCliente()).orElse(null));
        }

        double totalCalculado = 0.0;
        for (VentaRequestDTO.DetalleVentaDTO detDTO : request.getDetalles()) {
            Producto producto = productoDao.findById(detDTO.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            // Seguridad: el cajero solo puede cobrar productos de su propia organización
            if (empleado == null || empleado.getOrganizacion() == null
                    || producto.getOrganizacion() == null
                    || !producto.getOrganizacion().getIdOrganizacion().equals(empleado.getOrganizacion().getIdOrganizacion())) {
                throw new RuntimeException("No tienes permiso para vender ese producto.");
            }

            Integer stock = producto.getStock();
            if (stock != null && stock < detDTO.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

            double precioUnitario = precioVenta(producto);
            if ("SERVICIO".equalsIgnoreCase(producto.getTipo())
                    && detDTO.getPrecioUnitario() != null && detDTO.getPrecioUnitario() > 0) {
                precioUnitario = detDTO.getPrecioUnitario();
            }
            double subtotal = precioUnitario * detDTO.getCantidad();
            totalCalculado += subtotal;

            if (stock != null) {
                producto.setStock(stock - detDTO.getCantidad());
                productoDao.save(producto);
            }
            
            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(detDTO.getCantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotal);
            venta.getDetalles().add(detalle);
        }
        
        venta.setTotal(totalCalculado);
        return ventaDao.save(venta);
    }

    @Override
    public Venta encontrarPorId(Long id) {
        return ventaDao.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Venta procesarPedidoCliente(VentaRequestDTO request, Usuario cliente) {
        Venta venta = new Venta();
        venta.setFechaHora(LocalDateTime.now());
        venta.setMetodoPago(request.getMetodoPago());
        venta.setEstado("PENDIENTE");
        venta.setCliente(cliente);

        double totalCalculado = 0.0;
        for (VentaRequestDTO.DetalleVentaDTO detDTO : request.getDetalles()) {
            Producto producto = productoDao.findById(detDTO.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            
            Integer stock = producto.getStock();
            if (stock != null && stock < detDTO.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

            double precioUnitario = precioVenta(producto);
            if ("SERVICIO".equalsIgnoreCase(producto.getTipo())
                    && detDTO.getPrecioUnitario() != null && detDTO.getPrecioUnitario() > 0) {
                precioUnitario = detDTO.getPrecioUnitario();
            }
            double subtotal = precioUnitario * detDTO.getCantidad();
            totalCalculado += subtotal;

            if (stock != null) {
                producto.setStock(stock - detDTO.getCantidad());
                productoDao.save(producto);
            }
            
            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(detDTO.getCantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotal);
            venta.getDetalles().add(detalle);
        }
        
        venta.setTotal(totalCalculado);
        return ventaDao.save(venta);
    }

    private double precioVenta(Producto producto) {
        if (producto.getPrecioTotal() != null) {
            return producto.getPrecioTotal();
        }
        if (producto.getPrecio() != null) {
            return producto.getPrecio();
        }
        return 0.0;
    }

    @Override
    public List<Venta> listarVentas() {
        return ventaDao.findAll();
    }

    @Override
    public List<Venta> listarPedidosPendientes() {
        try {
            return ventaDao.findByEstado("PENDIENTE");
        } catch (Exception e) {
            return ventaDao.findAll().stream()
                    .filter(v -> "PENDIENTE".equalsIgnoreCase(v.getEstado()))
                    .toList();
        }
    }

    @Override
    public List<Venta> listarHistorialVentasAdmin() {
        return ventaDao.findAll();
    }

    @Override
    public List<Venta> listarPedidosCliente(Usuario cliente) {
        try {
            return ventaDao.findByCliente(cliente);
        } catch (Exception e) {
            return ventaDao.findAll().stream()
                    .filter(v -> v.getCliente() != null && v.getCliente().getIdUsuario().equals(cliente.getIdUsuario()))
                    .toList();
        }
    }

    @Override
    @Transactional
    public Venta marcarPedidoCompletado(Long idVenta) {
        Venta venta = encontrarPorId(idVenta);
        if (venta != null) {
            venta.setEstado("COMPLETADA");
            Venta guardada = ventaDao.save(venta);
            liberarOrdenServicio(venta);
            return guardada;
        }
        throw new RuntimeException("Pedido no encontrado con ID: " + idVenta);
    }

    private void liberarOrdenServicio(Venta venta) {
        if (venta == null || venta.getOrdenServicioId() == null) {
            return;
        }
        ordenServicioDao.findById(venta.getOrdenServicioId()).ifPresent(orden -> {
            orden.setOperarioAsignado(null);
            ordenServicioDao.save(orden);
        });
    }

    @Override
    public boolean ventaPerteneceAOrganizacion(Venta venta, Long idOrganizacion) {
        if (venta == null || idOrganizacion == null) {
            return false;
        }
        if (venta.getEmpleado() != null && venta.getEmpleado().getOrganizacion() != null) {
            if (venta.getEmpleado().getOrganizacion().getIdOrganizacion().equals(idOrganizacion)) {
                return true;
            }
        }
        if (venta.getDetalles() != null) {
            return venta.getDetalles().stream().anyMatch(d ->
                d.getProducto() != null &&
                d.getProducto().getOrganizacion() != null &&
                d.getProducto().getOrganizacion().getIdOrganizacion().equals(idOrganizacion)
            );
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> listarVentasPorOrganizacion(Long idOrganizacion, String estado) {
        return ventaDao.findAll().stream()
                .filter(v -> ventaPerteneceAOrganizacion(v, idOrganizacion))
                .filter(v -> estado == null || estado.equalsIgnoreCase(v.getEstado()))
                .toList();
    }
}