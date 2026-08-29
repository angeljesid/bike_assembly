package com.bike_assembly.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orden_servicio")
public class OrdenServicio implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Long idOrden;

    @Column(name = "cliente_nombre", nullable = false, length = 100)
    private String clienteNombre;

    @Column(name = "cliente_telefono", length = 30)
    private String clienteTelefono;

    @Column(name = "cliente_cedula", length = 30)
    private String clienteCedula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario clienteUsuario;

    @Column(name = "bicicleta_marca", length = 100)
    private String bicicletaMarca;

    @Column(name = "bicicleta_modelo", length = 100)
    private String bicicletaModelo;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(name = "precio_servicio")
    private Double precioServicio;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<RepuestoOrdenServicio> repuestos = new ArrayList<>();

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(nullable = false, length = 30)
    private String estado = EstadoOrdenServicio.EN_COLA.name();

    @Column(name = "facturado")
    private Boolean facturado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_operario")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario operarioAsignado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_registro")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario registradoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_organizacion")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Organizacion organizacion;

    public OrdenServicio() {
    }

    public Long getIdOrden() { return idOrden; }
    public void setIdOrden(Long idOrden) { this.idOrden = idOrden; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getClienteTelefono() { return clienteTelefono; }
    public void setClienteTelefono(String clienteTelefono) { this.clienteTelefono = clienteTelefono; }

    public String getClienteCedula() { return clienteCedula; }
    public void setClienteCedula(String clienteCedula) { this.clienteCedula = clienteCedula; }

    public Usuario getClienteUsuario() { return clienteUsuario; }
    public void setClienteUsuario(Usuario clienteUsuario) { this.clienteUsuario = clienteUsuario; }

    public String getBicicletaMarca() { return bicicletaMarca; }
    public void setBicicletaMarca(String bicicletaMarca) { this.bicicletaMarca = bicicletaMarca; }

    public String getBicicletaModelo() { return bicicletaModelo; }
    public void setBicicletaModelo(String bicicletaModelo) { this.bicicletaModelo = bicicletaModelo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getPrecioServicio() { return precioServicio; }
    public void setPrecioServicio(Double precioServicio) { this.precioServicio = precioServicio; }

    public List<RepuestoOrdenServicio> getRepuestos() { return repuestos; }
    public void setRepuestos(List<RepuestoOrdenServicio> repuestos) { this.repuestos = repuestos; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isFacturado() {
        return facturado != null && facturado;
    }
    public void setFacturado(boolean facturado) { this.facturado = facturado; }
    public void setFacturado(Boolean facturado) { this.facturado = facturado; }

    public Usuario getOperarioAsignado() { return operarioAsignado; }
    public void setOperarioAsignado(Usuario operarioAsignado) { this.operarioAsignado = operarioAsignado; }

    public Usuario getRegistradoPor() { return registradoPor; }
    public void setRegistradoPor(Usuario registradoPor) { this.registradoPor = registradoPor; }

    public Organizacion getOrganizacion() { return organizacion; }
    public void setOrganizacion(Organizacion organizacion) { this.organizacion = organizacion; }
}
