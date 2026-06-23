package com.proyecto.AccesoUsuarios.security;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

// Modulo: Servicio de autenticación
@Service
@RequiredArgsConstructor // <-- Genera el constructor público automático para los campos final, eliminando el warning de VS Code
public class UserDetailServiceImpl implements UserDetailsService {

    // Se eliminó @Autowired y se marcó como private final para asegurar la inmutabilidad del Bean
    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // 1. Buscar el usuario en la base de datos
        Usuario usuario = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // 2. Construir el objeto User de Spring Security con validación de estado
        // El tercer parámetro (boolean enabled) se mapea a tu campo 'habilitado'
        return new User(
            usuario.getUserName(),
            usuario.getPassword(),
            usuario.isHabilitado(), // Si es false, Spring Security bloquea el inicio de sesión automáticamente
            true,  // accountNonExpired (cuenta no expirada)
            true,  // credentialsNonExpired (contraseña no expirada)
            true,  // accountNonLocked (cuenta no bloqueada)
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol())) // Mapeo correcto del prefijo de autoridad
        );
    }
}