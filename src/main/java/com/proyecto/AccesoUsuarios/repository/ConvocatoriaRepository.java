package com.proyecto.AccesoUsuarios.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.AccesoUsuarios.model.Convocatoria;
import com.proyecto.AccesoUsuarios.model.Usuario;

@Repository
public interface ConvocatoriaRepository extends JpaRepository<Convocatoria, Long> {

    // 1. Para el estudiante: Ver las convocatorias filtradas por su estado exacto
    // ("ACTIVA")
    List<Convocatoria> findByEstado(String estado);

    // 2. Para el Dashboard Institución: Contar cuántas han creado (KPI)
    long countByCreador(Usuario creador);

    // 3. Para la lista "Mis Convocatorias" en el panel de la Institución
    List<Convocatoria> findByCreador(Usuario creador);

    // Para buscar una específica y asegurar pertenencia antes de edición o
    // eliminación
    Optional<Convocatoria> findByIdAndCreador(Long id, Usuario creador);

    // 4. Para el Dashboard Admin o métricas globales de estados activos
    long countByActivaTrue();

    // CORREGIDO: Filtra las convocatorias cuyo estado sea estrictamente 'ACTIVA' y
    // coincida con la palabra clave
    @Query("SELECT c FROM Convocatoria c WHERE c.estado = 'ACTIVA' AND " +
            "(LOWER(c.titulo) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Convocatoria> buscarPorKeyword(@Param("keyword") String keyword);

    // Lista las convocatorias del estudiante basándose en el flag booleano de
    // activación
    List<Convocatoria> findByActivaTrue();
}