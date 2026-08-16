package com.bike_assembly.services.venta;

import com.bike_assembly.domain.Venta;
import com.bike_assembly.domain.VentaRequestDTO; // Ajustado a tu ruta
import java.util.List;

public interface IVentaService {
    Venta procesarCobro(VentaRequestDTO request);
    List<Venta> listarVentas();
}
