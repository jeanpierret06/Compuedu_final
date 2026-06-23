package com.proyecto.AccesoUsuarios.repository;

import com.proyecto.AccesoUsuarios.model.Notificacion;
import com.proyecto.AccesoUsuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    // Crucial: Esto es lo que usa el Dashboard para mostrar la campanita
    List<Notificacion> findByUsuarioAndLeidaFalse(Usuario usuario);
}