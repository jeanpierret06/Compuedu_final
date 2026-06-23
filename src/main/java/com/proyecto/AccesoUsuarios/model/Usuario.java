package com.proyecto.AccesoUsuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "usuarios")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userName;

    @Column(name = "nombre_institucion", nullable = true) 
    private String nombreInstitucion;

    // CORREGIDO: Aseguramos explícitamente que permitan nulos para cuando se registre una Empresa
    @Column(name = "nombre", nullable = true)
    private String nombre;

    @Column(name = "apellido", nullable = true)
    private String apellido;

    @Column(name = "nivel_educativo", nullable = true)
    private String nivelEducativo;

    @Column(name = "estrato", nullable = true)
    private String estrato;

    @Column(nullable = false)
    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @Column(nullable = false)
    private String rol; 

    @Column(name="habilitado", nullable = false)
    private boolean habilitado = true;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "documento", nullable = false)
    private String documento;

    @Column(name = "telefono", nullable = false)
    private String telefono;

    @Column(name = "token_recuperacion")
    private String tokenRecuperacion;

    // =========================================================================
    // NUEVOS CAMPOS PARA AUDITORÍA Y VERIFICACIÓN
    // =========================================================================

    @Column(name = "documento_url", length = 255)
    private String documentoUrl;

    @Column(name = "estado", length = 50)
    private String estado = "PENDIENTE"; 

    // =========================================================================
    // MÉTODOS DE COMPORTAMIENTO INTERNO / LÓGICA DE NEGOCIO
    // =========================================================================

    public String getEstadoTexto() {
        if ("PENDIENTE".equals(this.estado)) return "En Revisión";
        if ("RECHAZADO".equals(this.estado)) return "Rechazado";
        return this.habilitado ? "Activo" : "Inactivo";
    }
}