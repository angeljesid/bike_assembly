package com.bike_assembly.services.venta;

import com.bike_assembly.domain.Venta;
import com.bike_assembly.domain.VentaRequestDTO;
import com.bike_assembly.domain.Usuario;
import java.util.List;

public interface IVentaService {
    Venta procesarCobro(VentaRequestDTO request);
    Venta encontrarPorId(Long id);
    Venta procesarPedidoCliente(VentaRequestDTO request, Usuario cliente);
    List<Venta> listarVentas();
    List<Venta> listarPedidosPendientes();
    List<Venta> listarHistorialVentasAdmin();
    List<Venta> listarPedidosCliente(Usuario cliente);
    Venta marcarPedidoCompletado(Long idVenta);
}