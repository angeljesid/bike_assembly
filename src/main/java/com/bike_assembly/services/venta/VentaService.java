package com.bike_assembly.services.venta;

import com.bike_assembly.domain.DetalleVenta;
import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.Venta;
import com.bike_assembly.domain.VentaRequestDTO; // Ajustado a tu ruta
import com.bike_assembly.repository.DetalleVentaDao;
import com.bike_assembly.repository.ProductoDao;
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
    private DetalleVentaDao detalleVentaDao;

    @Autowired
    private ProductoDao productoDao;

    @Override
    @Transactional
    public Venta procesarCobro(VentaRequestDTO request) {
        Venta venta = new Venta();
        venta.setFechaHora(LocalDateTime.now());
        venta.setMetodoPago(request.getMetodoPago());
        venta.setTotal(request.getTotal());
        Venta ventaGuardada = ventaDao.save(venta);

        for (VentaRequestDTO.DetalleRequestDTO det : request.getDetalles()) {
            Producto producto = productoDao.findById(det.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (producto.getStock() < det.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }
            
            producto.setStock(producto.getStock() - det.getCantidad());
            productoDao.save(producto);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(ventaGuardada);
            detalle.setProducto(producto);
            detalle.setCantidad(det.getCantidad());
            detalle.setPrecioUnitario(det.getPrecioUnitario());
            detalle.setSubtotal(det.getSubtotal());
            detalleVentaDao.save(detalle);
        }
        return ventaGuardada;
    }

    @Override
    public List<Venta> listarVentas() {
        return ventaDao.findAll();
    }
}