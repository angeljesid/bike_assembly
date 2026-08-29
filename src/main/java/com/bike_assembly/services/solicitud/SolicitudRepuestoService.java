package com.bike_assembly.services.solicitud;

import com.bike_assembly.domain.EstadoOrdenServicio;
import com.bike_assembly.domain.OrdenServicio;
import com.bike_assembly.domain.Organizacion;
import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.RepuestoOrdenServicio;
import com.bike_assembly.domain.SolicitudRepuesto;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.OrdenServicioDao;
import com.bike_assembly.repository.ProductoDao;
import com.bike_assembly.repository.SolicitudRepuestoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SolicitudRepuestoService implements ISolicitudRepuestoService {

    @Autowired
    private SolicitudRepuestoDao solicitudRepuestoDao;

    @Autowired
    private OrdenServicioDao ordenServicioDao;

    @Autowired
    private ProductoDao productoDao;

    @Override
    @Transactional
    public SolicitudRepuesto solicitar(Long idOrden, Producto producto, Integer cantidad, Usuario operario) {
        if (operario == null || operario.getOrganizacion() == null || producto == null) {
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
                || orden.getOperarioAsignado() == null
                || !orden.getOperarioAsignado().getIdUsuario().equals(operario.getIdUsuario())) {
            return null;
        }

        Optional<Producto> prodOpt = productoDao.findById(producto.getIdProducto());
        if (prodOpt.isEmpty() || Boolean.TRUE.equals(prodOpt.get().getEliminado())) {
            return null;
        }
        Producto prod = prodOpt.get();

        SolicitudRepuesto solicitud = new SolicitudRepuesto();
        solicitud.setOrden(orden);
        solicitud.setProducto(prod);
        solicitud.setCantidad(cantidad != null && cantidad > 0 ? cantidad : 1);
        double precio = prod.getPrecioTotal() != null
                ? prod.getPrecioTotal()
                : (prod.getPrecio() != null ? prod.getPrecio() : 0.0);
        solicitud.setPrecioUnitario(precio);
        solicitud.setSubtotal(precio * solicitud.getCantidad());
        solicitud.setEstado(SolicitudRepuesto.PENDIENTE);
        solicitud.setSolicitadoPor(operario);
        solicitud.setOrganizacion(orden.getOrganizacion());
        return solicitudRepuestoDao.save(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudRepuesto> listarPendientes(Organizacion organizacion) {
        if (organizacion == null) {
            return List.of();
        }
        return solicitudRepuestoDao.findByOrganizacionAndEstado(organizacion, SolicitudRepuesto.PENDIENTE);
    }

    @Override
    @Transactional
    public boolean aceptar(Long idSolicitud, Usuario empleado) {
        Optional<SolicitudRepuesto> solOpt = solicitudRepuestoDao.findById(idSolicitud);
        if (solOpt.isEmpty()) {
            return false;
        }
        SolicitudRepuesto sol = solOpt.get();
        if (!SolicitudRepuesto.PENDIENTE.equals(sol.getEstado())) {
            return false;
        }

        OrdenServicio orden = sol.getOrden();
        if (orden.isFacturado()) {
            return false;
        }

        RepuestoOrdenServicio rep = new RepuestoOrdenServicio();
        rep.setOrden(orden);
        rep.setProducto(sol.getProducto());
        rep.setCantidad(sol.getCantidad());
        rep.setPrecioUnitario(sol.getPrecioUnitario());
        rep.setSubtotal(sol.getSubtotal());
        orden.getRepuestos().add(rep);

        orden.setEstado(EstadoOrdenServicio.EN_REPARACION.name());
        ordenServicioDao.save(orden);

        sol.setEstado(SolicitudRepuesto.ACEPTADA);
        solicitudRepuestoDao.save(sol);
        return true;
    }

    @Override
    @Transactional
    public boolean rechazar(Long idSolicitud, Usuario empleado) {
        Optional<SolicitudRepuesto> solOpt = solicitudRepuestoDao.findById(idSolicitud);
        if (solOpt.isEmpty()) {
            return false;
        }
        SolicitudRepuesto sol = solOpt.get();
        if (!SolicitudRepuesto.PENDIENTE.equals(sol.getEstado())) {
            return false;
        }

        OrdenServicio orden = sol.getOrden();
        if (!orden.isFacturado()) {
            orden.setEstado(EstadoOrdenServicio.EN_REPARACION.name());
            ordenServicioDao.save(orden);
        }

        sol.setEstado(SolicitudRepuesto.RECHAZADA);
        solicitudRepuestoDao.save(sol);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudRepuesto> listarPorOrden(OrdenServicio orden) {
        return solicitudRepuestoDao.findByOrden(orden);
    }
}
