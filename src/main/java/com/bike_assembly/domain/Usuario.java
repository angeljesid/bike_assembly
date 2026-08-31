package com.bike_assembly.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String cedula;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(nullable = false)
    private Boolean activo = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    private String resetPasswordToken;
    private LocalDateTime tokenExpirationDate;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "plan_solicitado", length = 50)
    private String planSolicitado;

    @Column(name = "plan_estado", length = 20)
    private String planEstado;

    @Column(name = "precio_plan_solicitado")
    private BigDecimal precioPlanSolicitado;

    @Column(name = "fecha_solicitud_plan")
    private LocalDateTime fechaSolicitudPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_organizacion")
    private Organizacion organizacion;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductoGuardado> productosGuardados = new ArrayList<>();

    public Usuario() {}

    public Long getIdUsuario() {
        return idUsuario; 
        }
    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
        }

    public String getUsername() {
        return username; 
    }
    public void setUsername(String username) {
        this.username = username; 
    }

    public String getPassword() {
        return password; 
        }
    public void setPassword(String password) {
        this.password = password; 
        }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public String getResetPasswordToken() { return resetPasswordToken; }
    public void setResetPasswordToken(String resetPasswordToken) { this.resetPasswordToken = resetPasswordToken; }

    public LocalDateTime getTokenExpirationDate() { return tokenExpirationDate; }
    public void setTokenExpirationDate(LocalDateTime tokenExpirationDate) { this.tokenExpirationDate = tokenExpirationDate; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Organizacion getOrganizacion() { return organizacion; }
    public void setOrganizacion(Organizacion organizacion) { this.organizacion = organizacion; }

    public List<ProductoGuardado> getProductosGuardados() { return productosGuardados; }
    public void setProductosGuardados(List<ProductoGuardado> productosGuardados) { this.productosGuardados = productosGuardados; }

    public String getPlanSolicitado() { return planSolicitado; }
    public void setPlanSolicitado(String planSolicitado) { this.planSolicitado = planSolicitado; }

    public String getPlanEstado() { return planEstado; }
    public void setPlanEstado(String planEstado) { this.planEstado = planEstado; }

    public BigDecimal getPrecioPlanSolicitado() { return precioPlanSolicitado; }
    public void setPrecioPlanSolicitado(BigDecimal precioPlanSolicitado) { this.precioPlanSolicitado = precioPlanSolicitado; }

    public LocalDateTime getFechaSolicitudPlan() { return fechaSolicitudPlan; }
    public void setFechaSolicitudPlan(LocalDateTime fechaSolicitudPlan) { this.fechaSolicitudPlan = fechaSolicitudPlan; }
}