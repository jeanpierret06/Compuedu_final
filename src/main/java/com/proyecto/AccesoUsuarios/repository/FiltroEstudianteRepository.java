package com.proyecto.AccesoUsuarios.repository;

import com.proyecto.AccesoUsuarios.model.FiltroEstudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FiltroEstudianteRepository extends JpaRepository<FiltroEstudiante, Long> {

    // CORREGIDO: Consulta JPQL optimizada y simplificada sin alias redundantes para asegurar compatibilidad nativa
    @Query("SELECT f.categoria, COUNT(f) FROM FiltroEstudiante f GROUP BY f.categoria ORDER BY COUNT(f) DESC")
    List<InteresAgrupado> findTopInteresesGlobales();

    /**
     * Interfaz de proyección basada en firmas (Closed Projection).
     * Mapea de forma automática y asíncrona las columnas del set de resultados de la base de datos.
     */
    interface InteresAgrupado {
        String getCategoria();
        Long getCantidad();
    }
}