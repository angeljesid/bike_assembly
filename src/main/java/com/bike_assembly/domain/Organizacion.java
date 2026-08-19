package com.bike_assembly.domain;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "organizacion")
public class Organizacion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_organizacion")
    private Long idOrganizacion;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false)
    private String direccion;

    public Organizacion() {
    }

    public Organizacion(String nombre, String telefono, String direccion) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    public Long getIdOrganizacion() {
        return idOrganizacion;
    }

    public void setIdOrganizacion(Long idOrganizacion) {
        this.idOrganizacion = idOrganizacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}