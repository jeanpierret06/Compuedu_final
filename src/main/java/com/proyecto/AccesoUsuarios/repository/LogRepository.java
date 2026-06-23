package com.proyecto.AccesoUsuarios.repository;

import com.proyecto.AccesoUsuarios.model.LogSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<LogSystem, Long> {
    
    /**
     * Recupera la totalidad de los registros de auditoría técnica persistidos en la base de datos,
     * ordenándolos cronológicamente de forma descendente (los eventos más recientes aparecerán primero).
     * * @return Una lista ordenada de objetos LogSystem.
     */
    List<LogSystem> findAllByOrderByFechaHoraDesc();
}