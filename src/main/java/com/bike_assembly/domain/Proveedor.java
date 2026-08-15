package com.bike_assembly.domain;

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
    private String tipoEstablecimiento; // "Empresa" o "Bodega"
    
    private String direccion;

    // Relación Inversa (Opcional pero recomendada): Un proveedor puede tener muchos productos
    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL)
    private List<Producto> productos;

    public Proveedor() {}

    // Getters y Setters
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
}