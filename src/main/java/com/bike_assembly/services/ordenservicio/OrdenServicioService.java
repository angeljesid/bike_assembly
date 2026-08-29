package com.bike_assembly.services.ordenservicio;

import com.bike_assembly.domain.EstadoOrdenServicio;
import com.bike_assembly.domain.OrdenServicio;
import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.RepuestoOrdenServicio;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.domain.Venta;
import com.bike_assembly.domain.VentaRequestDTO;
import com.bike_assembly.repository.OrdenServicioDao;
import com.bike_assembly.repository.ProductoDao;
import com.bike_assembly.repository.VentaDao;
import com.bike_assembly.services.venta.IVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrdenServicioService implements IOrdenServicioService {

    private static final String CODIGO_MANO_DE_OBRA = "MANO-DE-OBRA";

    @Autowired
    private OrdenServicioDao ordenServicioDao;

    @Autowired
    private ProductoDao productoDao;

    @Autowired
    private VentaDao ventaDao;

    @Autowired
    private IVentaService ventaService;

    @Override
    @Transactional
    public OrdenServicio registrar(OrdenServicio orden, Usuario registrador) {
        orden.setOrganizacion(registrador.getOrganizacion());
        orden.setRegistradoPor(registrador);
        orden.setEstado(EstadoOrdenServicio.EN_COLA.name());
        if (orden.getRepuestos() != null) {
            for (RepuestoOrdenServicio r : orden.getRepuestos()) {
                r.setOrden(orden);
            }
        }
        return ordenServicioDao.save(orden);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenServicio> listarEnCola(Organizacion organizacion) {
        if (organizacion == null) {
            return List.of();
        }
        return ordenServicioDao.findByOrganizacionAndEstado(organizacion, EstadoOrdenServicio.EN_COLA.name());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenServicio> listarPorOperario(Usuario operario) {
        if (operario == null) {
            return List.of();
        }
        return ordenServicioDao.findByOperarioAsignado(operario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenServicio> listarPorCliente(Usuario cliente) {
        if (cliente == null) {
            return List.of();
        }
        return ordenServicioDao.findByClienteUsuarioAndFacturadoFalse(cliente);
    }

    @Override
    @Transactional
    public boolean tomarOrden(Long idOrden, Usuario operario) {
        if (operario == null || operario.getOrganizacion() == null) {
            return false;
        }
        Optional<OrdenServicio> ordenOpt = ordenServicioDao.findById(idOrden);
        if (ordenOpt.isEmpty()) {
            return false;
        }
        OrdenServicio orden = ordenOpt.get();
        Long orgId = operario.getOrganizacion().getIdOrganizacion();
        if (orden.getOrganizacion() == null
                || !orden.getOrganizacion().getIdOrganizacion().equals(orgId)
                || !EstadoOrdenServicio.EN_COLA.name().equals(orden.getEstado())
                || orden.getOperarioAsignado() != null) {
            return false;
        }
        orden.setOperarioAsignado(operario);
        orden.setEstado(EstadoOrdenServicio.EN_DIAGNOSTICO.name());
        ordenServicioDao.save(orden);
        return true;
    }

    @Override
    @Transactional
    public boolean actualizarEstado(Long idOrden, String nuevoEstado, Usuario operario) {
        if (operario == null || operario.getOrganizacion() == null) {
            return false;
        }
        Optional<OrdenServicio> ordenOpt = ordenServicioDao.findById(idOrden);
        if (ordenOpt.isEmpty()) {
            return false;
        }
        OrdenServicio orden = ordenOpt.get();
        Long orgId = operario.getOrganizacion().getIdOrganizacion();
        if (orden.getOrganizacion() == null
                || !orden.getOrganizacion().getIdOrganizacion().equals(orgId)
                || orden.getOperarioAsignado() == null
                || !orden.getOperarioAsignado().getIdUsuario().equals(operario.getIdUsuario())) {
            return false;
        }
        orden.setEstado(nuevoEstado);
        ordenServicioDao.save(orden);
        return true;
    }

    @Override
    @Transactional
    public Venta marcarListoEntrega(Long idOrden, Usuario operario) {
        if (operario == null || operario.getOrganizacion() == null) {
            return null;
        }
        Optional<OrdenServicio> ordenOpt = ordenServicioDao.findById(idOrden);
        if (ordenOpt.isEmpty()) {
            return null;
        }
        OrdenServicio orden = ordenOpt.get();
        Long orgId = operario.getOrganizacion().getIdOrganizacion();
        if (orden.getOrganizacion() == null
                || !orden.getOrganizacion().getIdOrganizacion().equals(orgId)
                || !EstadoOrdenServicio.TERMINADO.name().equals(orden.getEstado())
                || orden.isFacturado()) {
            return null;
        }
        Venta venta = facturarServicio(orden, operario);
        if (venta != null) {
            orden.setFacturado(true);
            ordenServicioDao.save(orden);
        }
        return venta;
    }

    @Override
    @Transactional
    public Venta facturarServicio(OrdenServicio orden, Usuario empleado) {
        if (orden == null || orden.getOrganizacion() == null) {
            return null;
        }
        List<VentaRequestDTO.DetalleVentaDTO> detalles = new ArrayList<>();
        double total = 0.0;

        Producto manoObra = obtenerOCrearProductoManoDeObra(orden.getOrganizacion());

        double precioServicio = orden.getPrecioServicio() != null ? orden.getPrecioServicio() : 0.0;
        if (precioServicio > 0) {
            VentaRequestDTO.DetalleVentaDTO mo = new VentaRequestDTO.DetalleVentaDTO();
            mo.setIdProducto(manoObra.getIdProducto());
            mo.setCantidad(1);
            mo.setPrecioUnitario(precioServicio);
            mo.setSubtotal(precioServicio);
            detalles.add(mo);
            total += precioServicio;
        }

        if (orden.getRepuestos() != null) {
            for (RepuestoOrdenServicio r : orden.getRepuestos()) {
                if (r.getProducto() == null) {
                    continue;
                }
                VentaRequestDTO.DetalleVentaDTO dto = new VentaRequestDTO.DetalleVentaDTO();
                dto.setIdProducto(r.getProducto().getIdProducto());
                dto.setCantidad(r.getCantidad());
                dto.setPrecioUnitario(r.getPrecioUnitario());
                dto.setSubtotal(r.getSubtotal());
                detalles.add(dto);
                total += r.getSubtotal() != null ? r.getSubtotal() : 0.0;
            }
        }

        if (detalles.isEmpty()) {
            return null;
        }

        VentaRequestDTO request = new VentaRequestDTO();
        request.setTotal(total);
        request.setMetodoPago("EFECTIVO");
        if (orden.getClienteUsuario() != null) {
            request.setIdCliente(orden.getClienteUsuario().getIdUsuario());
        }
        request.setDetalles(detalles);

        Venta venta = ventaService.procesarPedidoCliente(request, orden.getClienteUsuario());
        if (venta != null && empleado != null) {
            venta.setEmpleado(empleado);
            venta.setOrdenServicioId(orden.getIdOrden());
            venta.setAutorMantenimiento(orden.getOperarioAsignado() != null
                    ? orden.getOperarioAsignado().getNombre()
                    : (empleado != null ? empleado.getNombre() : null));
            ventaDao.save(venta);
        }
        return venta;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenServicio> listarTodas(Organizacion organizacion) {
        if (organizacion == null) {
            return List.of();
        }
        return ordenServicioDao.findByOrganizacion(organizacion);
    }

    private Producto obtenerOCrearProductoManoDeObra(Organizacion organizacion) {
        Optional<Producto> existente = productoDao.findByCodigoBarrasAndOrganizacion(CODIGO_MANO_DE_OBRA, organizacion);
        if (existente.isPresent()) {
            return existente.get();
        }
        Producto producto = new Producto();
        producto.setNombre("Mano de Obra / Servicio");
        producto.setCodigoBarras(CODIGO_MANO_DE_OBRA);
        producto.setTipo("SERVICIO");
        producto.setCategoria("servicio");
        producto.setStock(null);
        producto.setPrecio(0.0);
        producto.setPrecioInicial(0.0);
        producto.setPrecioTotal(0.0);
        producto.setIva(0.0);
        producto.setPublicado(false);
        producto.setUnidadesPublicadas(0);
        producto.setOrganizacion(organizacion);
        return productoDao.save(producto);
    }
}
