package com.bike_assembly.domain;

import java.util.List;

public class VentaRequestDTO {
    
    private String metodoPago;
    private Double total;
    private Long idCliente;
    private List<DetalleVentaDTO> detalles;

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }

    public List<DetalleVentaDTO> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVentaDTO> detalles) { this.detalles = detalles; }

    public static class DetalleVentaDTO {
        private Long idProducto;
        private Integer cantidad;
        private Double precioUnitario;
        private Double subtotal;
        
        public Long getIdProducto() { return idProducto; }
        public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }

        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

        public Double getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }

        public Double getSubtotal() { return subtotal; }
        public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    }
}