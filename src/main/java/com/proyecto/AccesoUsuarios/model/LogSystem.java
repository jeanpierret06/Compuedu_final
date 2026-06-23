package com.proyecto.AccesoUsuarios.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "logs")
public class LogSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String accion;

    @Column(columnDefinition = "TEXT")
    private String detalle;

    @Column(nullable = false)
    private String usuario; // Guarda el username del administrador que hizo la acción

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false)
    private String tipo; // INFO, SUCCESS, WARNING, DANGER

    // Constructor por defecto
    public LogSystem() {}

    // Constructor práctico
    public LogSystem(String accion, String detalle, String usuario, String tipo) {
        this.accion = accion;
        this.detalle = detalle;
        this.usuario = usuario;
        this.tipo = tipo;
        this.fechaHora = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}