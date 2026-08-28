package com.bike_assembly.services.venta;

import com.bike_assembly.domain.DetalleVenta;
import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.domain.Venta;
import com.bike_assembly.domain.VentaRequestDTO;
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
    
    @Override
    @Transactional
    public Venta procesarCobro(VentaRequestDTO request, Usuario empleado) {
        Venta venta = new Venta();
        venta.setFechaHora(LocalDateTime.now());
        venta.setMetodoPago(request.getMetodoPago());
        venta.setTotal(request.getTotal());
        venta.setEstado("COMPLETADA");
        venta.setEmpleado(empleado);

        if (request.getIdCliente() != null) {
            venta.setCliente(usuarioDao.findById(request.getIdCliente()).orElse(null));
        }

        for (VentaRequestDTO.DetalleVentaDTO detDTO : request.getDetalles()) {
            Producto producto = productoDao.findById(detDTO.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            
            if(producto.getStock() != null && producto.getStock() < detDTO.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }
            
            if(producto.getStock() != null) {
                producto.setStock(producto.getStock() - detDTO.getCantidad());
                productoDao.save(producto);
            }
            
            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(detDTO.getCantidad());
            detalle.setPrecioUnitario(detDTO.getPrecioUnitario());
            detalle.setSubtotal(detDTO.getSubtotal());
            venta.getDetalles().add(detalle);
        }
        
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
        venta.setTotal(request.getTotal());
        venta.setEstado("PENDIENTE");
        venta.setCliente(cliente);

        for (VentaRequestDTO.DetalleVentaDTO detDTO : request.getDetalles()) {
            Producto producto = productoDao.findById(detDTO.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            
            if(producto.getStock() != null && producto.getStock() < detDTO.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }
            
            if(producto.getStock() != null) {
                producto.setStock(producto.getStock() - detDTO.getCantidad());
                productoDao.save(producto);
            }
            
            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(detDTO.getCantidad());
            detalle.setPrecioUnitario(detDTO.getPrecioUnitario());
            detalle.setSubtotal(detDTO.getSubtotal());
            venta.getDetalles().add(detalle);
        }
        
        return ventaDao.save(venta);
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
            return ventaDao.save(venta);
        }
        throw new RuntimeException("Pedido no encontrado con ID: " + idVenta);
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