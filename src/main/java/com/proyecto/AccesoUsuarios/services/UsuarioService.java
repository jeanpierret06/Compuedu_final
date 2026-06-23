package com.proyecto.AccesoUsuarios.services;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // <-- Automatiza el constructor público con todas las variables marcadas como final
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder passwordEncoder; // <-- Modificado a la interfaz general inyectada en SecurityConfig

    // --- MÉTODOS REQUERIDOS POR EL CONTROLADOR ---

    // 1. Guardar usuario (Sirve para crear y actualizar)
    public void save(Usuario usuario) {
        // Validación preventiva en creación pura de usuario
        if (usuario.getId() == null) {
            if (usuario.getPassword() == null || usuario.getPassword().length() < 6) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
            }
            // Encriptación inicial y estado por defecto para nuevos registros
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            usuario.setHabilitado(false); // Inicia deshabilitado hasta verificación de documentos en registro público
        } else {
            // [CORRECCIÓN CRÍTICA] Si viene de una edición, validamos si la contraseña ya es un hash BCrypt (empieza con $2a$)
            // Esto evita re-encriptar un hash existente cuando el controlador guarda los cambios generales
            if (usuario.getPassword() != null && !usuario.getPassword().startsWith("$2a$")) {
                if (usuario.getPassword().length() < 6) {
                    throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres");
                }
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            }
        }
        
        repo.save(usuario);
    }

    // 2. Listar todos (Usado en el panel Admin y PDF)
    public List<Usuario> listarUsuarios() {
        // Redirige al método de filtrado personalizado enviando parámetros nulos para traer todo
        return repo.filtrarUsuarios(null, null);
    }

    // NUEVO MÉTODO: Para listar con filtro de palabra clave
    public List<Usuario> listarUsuarios(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return repo.filtrarUsuarios(keyword, null); 
        }
        return repo.filtrarUsuarios(null, null);
    }

    // 3. Obtener un usuario por su ID (Usado para editar)
    public Usuario obtenerPorId(Long id) {
        return repo.findById(id).orElse(null); 
    }

    // 4. Eliminar usuario
    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    // --- TU MÉTODO DE FILTRADO ---
    public List<Usuario> filtrarUsuarios(String keyword, String rol) {
        String rolBusqueda = (rol == null || rol.equals("Todos") || rol.isEmpty()) ? null : rol;
        String keywordBusqueda = (keyword == null || keyword.isEmpty()) ? null : keyword;

        return repo.filtrarUsuarios(keywordBusqueda, rolBusqueda);
    }

    public boolean existsByUserName(String userName) {
        return repo.existsByUserName(userName);
    }

    public void cambiarEstadoUsuario(Long id) {
        Usuario usuario = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Bloqueo de seguridad para el admin principal
        if (usuario.getUserName().equals("manny")) {
            throw new RuntimeException("No está permitido inhabilitar al administrador principal del sistema.");
        }
        
        usuario.setHabilitado(!usuario.isHabilitado());
        repo.save(usuario);
    }

    public Usuario findByUserName(String username) {
        return repo.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el username: " + username));
    }
}