package com.bike_assembly.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "proveedor")
public class Proveedor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Long idProveedor;

    private String nombre;
    private String numero;
    private String correo;
    
    @Column(name = "tipo_establecimiento")
    private String tipoEstablecimiento; 
    
    private String direccion;

    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"proveedor", "hibernateLazyInitializer", "handler"})
    private List<Producto> productos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_organizacion")
    @JsonIgnoreProperties({"proveedores", "productos", "hibernateLazyInitializer", "handler"})
    private Organizacion organizacion;

    public Proveedor() {}

    public Long getIdProveedor() { return idProveedor; }
    public void setIdProveedor(Long idProveedor) { this.idProveedor = idProveedor; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTipoEstablecimiento() { return tipoEstablecimiento; }
    public void setTipoEstablecimiento(String tipoEstablecimiento) { this.tipoEstablecimiento = tipoEstablecimiento; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public List<Producto> getProductos() { return productos; }
    public void setProductos(List<Producto> productos) { this.productos = productos; }
    public Organizacion getOrganizacion() { return organizacion; }
    public void setOrganizacion(Organizacion organizacion) { this.organizacion = organizacion; }
}