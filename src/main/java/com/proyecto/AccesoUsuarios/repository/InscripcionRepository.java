package com.proyecto.AccesoUsuarios.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.AccesoUsuarios.model.Convocatoria;
import com.proyecto.AccesoUsuarios.model.Inscripcion;
import com.proyecto.AccesoUsuarios.model.Usuario;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    
    // 1. Busca las inscripciones vinculadas a una entidad Usuario completa (usado en el home del estudiante)
    List<Inscripcion> findByUsuario(Usuario usuario);
    
    // 2. Valida si un estudiante ya se encuentra postulado a una convocatoria específica
    boolean existsByUsuarioAndConvocatoria(Usuario usuario, Convocatoria convocatoria);

    // 3. Métodos basados en propiedades anidadas (estructura: Entidad_PropiedadRelación)
    // Cuenta las inscripciones de todas las convocatorias pertenecientes a una institución específica (KPI)
    long countByConvocatoria_Creador(Usuario creador);
    
    // Lista las inscripciones de las convocatorias que fueron publicadas por la institución
    List<Inscripcion> findByConvocatoria_Creador(Usuario creador);

    // CORREGIDO: Declaración nativa por convención de Spring Data que enlaza la propiedad 'usuario' y su 'id'
    // Sincroniza perfectamente con la llamada del controlador REST sin requerir anotaciones @Query
    List<Inscripcion> findByUsuario_Id(Long usuarioId);

    // 4. Cuenta el volumen total de postulantes que posee una convocatoria en particular
    long countByConvocatoriaId(Long convocatoriaId);
    
    // NOTA: Se eliminó el método 'findAll()' porque ya es heredado por defecto desde JpaRepository
}