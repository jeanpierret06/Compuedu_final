package com.proyecto.AccesoUsuarios.model;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "convocatorias")
public class Convocatoria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String titulo;

    @Column(columnDefinition = "TEXT") 
    private String descripcion;

    @Column(columnDefinition = "TEXT") 
    private String requisitos; // ¡Añadido para soportar la vista de detalle!

    private String categoria;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean activa = true;
    private String estado = "ACTIVA";
    
    // ¡CORREGIDO!: Ahora es un campo real mapeado en la base de datos
    private int vacantes; 

    // RELACIÓN NECESARIA PARA EL DASHBOARD
    @ManyToOne
    @JoinColumn(name = "creador_id") 
    private Usuario creador;

    // --- NUEVOS CAMPOS ADICIONALES PARA MULTIMEDIA Y GEOLOCALIZACIÓN ---
    @Column(name = "imagen_url")
    private String imagenUrl;

    private Double latitud;
    private Double longitud;

    // ==========================================
    // GETTERS Y SETTERS TRADICIONALES
    // ==========================================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getRequisitos() { return requisitos; }
    public void setRequisitos(String requisitos) { this.requisitos = requisitos; }
    
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Usuario getCreador() { return creador; }
    public void setCreador(Usuario creador) { this.creador = creador; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    // ¡CORREGIDO!: Getters y Setters reales para el flujo de cupos
    public int getVacantes() {
        return this.vacantes;
    }
    
    public void setVacantes(int vacantes) {
        this.vacantes = vacantes;
    }

    // --- GETTERS Y SETTERS NUEVOS ---
    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }
}