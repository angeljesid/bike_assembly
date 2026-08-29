package com.bike_assembly.domain;

public class ProductoCatalogoDTO {

    private Long idProducto;
    private String nombre;
    private Double precio;
    private Double precioTotal;
    private String tipo;
    private Integer disponibilidad;
    private String marca;
    private String categoria;
    private String condicion;
    private String imagen;
    private String codigoBarras;
    private String nombreOrganizacion;
    private String telefonoOrganizacion;
    private Long idOrganizacion;

    public ProductoCatalogoDTO() {
    }

    public ProductoCatalogoDTO(Producto p) {
        this.idProducto = p.getIdProducto();
        this.nombre = p.getNombre();
        this.precio = p.getPrecio();
        this.precioTotal = p.getPrecioTotal();
        this.tipo = p.getTipo();
        Integer disponibles = p.getUnidadesPublicadas() != null ? p.getUnidadesPublicadas() : 0;
        this.disponibilidad = disponibles > 0 ? disponibles : 0;
        this.marca = p.getMarca();
        this.categoria = p.getCategoria();
        this.condicion = p.getCondicion();
        this.imagen = p.getImagen();
        this.codigoBarras = p.getCodigoBarras();
        if (p.getOrganizacion() != null) {
            this.nombreOrganizacion = p.getOrganizacion().getNombre();
            this.telefonoOrganizacion = p.getOrganizacion().getTelefono();
            this.idOrganizacion = p.getOrganizacion().getIdOrganizacion();
        }
    }

    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public Double getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(Double precioTotal) { this.precioTotal = precioTotal; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Integer getDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(Integer disponibilidad) { this.disponibilidad = disponibilidad; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getCondicion() { return condicion; }
    public void setCondicion(String condicion) { this.condicion = condicion; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getNombreOrganizacion() { return nombreOrganizacion; }
    public void setNombreOrganizacion(String nombreOrganizacion) { this.nombreOrganizacion = nombreOrganizacion; }

    public String getTelefonoOrganizacion() { return telefonoOrganizacion; }
    public void setTelefonoOrganizacion(String telefonoOrganizacion) { this.telefonoOrganizacion = telefonoOrganizacion; }

    public Long getIdOrganizacion() { return idOrganizacion; }
    public void setIdOrganizacion(Long idOrganizacion) { this.idOrganizacion = idOrganizacion; }
}
