package com.proyecto.AccesoUsuarios.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.AccesoUsuarios.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("SELECT u FROM Usuario u WHERE " +
            "(:rol IS NULL OR :rol = '' OR :rol = 'Todos' OR u.rol = :rol) AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "u.documento LIKE CONCAT('%', :keyword, '%'))")
    List<Usuario> filtrarUsuarios(@Param("keyword") String keyword, @Param("rol") String rol);

    @Query("SELECT u FROM Usuario u WHERE "
            + "CONCAT(u.nombre, ' ', u.apellido, ' ', u.email, ' ', u.documento) "
            + "LIKE %?1%")
    public List<Usuario> findAll(String keyword);

    // Spring buscará automáticamente por el campo 'userName'
    Optional<Usuario> findByUserName(String userName);

    // 2. SOLUCIÓN AL ERROR: findByHabilitadoFalse
    // Busca usuarios donde habilitado = false
    List<Usuario> findByHabilitadoFalse();

    // 3. SOLUCIÓN AL ERROR: countByHabilitadoFalse
    // Cuenta cuántos usuarios hay inactivos (para el dashboard)
    long countByHabilitadoFalse();

    // (Opcional) Método útil si necesitas validar registro por email
    Optional<Usuario> findByEmail(String email);

    // (Opcional) Para verificar si existe antes de guardar
    boolean existsByUserName(String userName);

    // Para buscar al usuario cuando hace clic en el link del correo
    Optional<Usuario> findByTokenRecuperacion(String tokenRecuperacion);
    // poder buscar usuarios en la base de datos usando su correo o el token que
    // llega desde el link del email

    long countByEstado(String estado);

    List<Usuario> findByEstado(String estado);

    // Cambiar de 'public void save(Usuario usuario)' a:
}
