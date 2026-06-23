package com.proyecto.AccesoUsuarios.model;

import jakarta.persistence.*;
import lombok.Data; // Si usas Lombok, esto ahorra los getters/setters

@Entity
@Table(name = "filtros_estudiante")
@Data // Esto quita los warnings de "field is not used"
public class FiltroEstudiante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    
    private String palabraClave;
    private String categoria;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}