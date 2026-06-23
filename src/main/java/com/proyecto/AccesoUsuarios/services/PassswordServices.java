package com.proyecto.AccesoUsuarios.services;

import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio encargado de la gestión de contraseñas y recuperación de cuentas.
 * Coordina la lógica de negocio entre el backend en Java y el microservicio de Python.
 */
@Service
@RequiredArgsConstructor // <-- Genera automáticamente el constructor público con todos los campos declarados como final
public class PassswordServices {

    // Se eliminan los @Autowired individuales y se declaran como variables finales inmutables
    private final RestTemplate restTemplate;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // [NUEVO] Inyección dinámica de la URL del Servidor Spring (Nube o Local)
    @Value("${app.url.base:http://localhost:8080}")
    private String urlBaseSpring;

    // [NUEVO] Inyección dinámica de la URL del Microservicio de Python (Nube o Local)
    @Value("${app.url.python:http://localhost:5000}")
    private String urlBasePython;

    public void solicitarRestablecimiento(String email) {
        // 1. Buscar al usuario por email
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            // 2. Generar y guardar el token en la base de datos
            String token = UUID.randomUUID().toString();
            usuario.setTokenRecuperacion(token);
            usuarioRepository.save(usuario);

            // 3. Preparar el link para el correo dinámicamente usando la propiedad externa
            String linkDeRecuperacion = urlBaseSpring + "/auth/reset-password?token=" + token;

            // 4. Enviar datos a Python
            Map<String, String> datosParaPython = new HashMap<>();
            datosParaPython.put("email", email);
            
            // CORRECCIÓN DEFINITIVA: Cambiar "enlace" a "link" para que coincida con app.py
            datosParaPython.put("link", linkDeRecuperacion); 

            // Se utiliza la URL inyectada dinámicamente evitando el localhost fijo
            String pythonApiUrl = urlBasePython + "/api/enviar-enlace";

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                HttpEntity<Map<String, String>> request = new HttpEntity<>(datosParaPython, headers);
                restTemplate.postForEntity(pythonApiUrl, request, String.class);
                
                System.out.println("Correo de recuperación solicitado para: " + email);
            } catch (Exception e) {
                System.err.println("Error enviando a Python: " + e.getMessage());
            }
        } else {
            System.err.println("No se encontró usuario con el email: " + email);
        }
    }

    public boolean cambiarPasswordConToken(String token, String nuevaPassword) {
        // 1. Buscar al usuario que tiene ese token específico
        Optional<Usuario> usuarioOpt = usuarioRepository.findByTokenRecuperacion(token);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            // 2. Encriptar la nueva contraseña
            usuario.setPassword(passwordEncoder.encode(nuevaPassword));

            // 3. Limpiar el token para que no se pueda volver a usar
            usuario.setTokenRecuperacion(null);

            // 4. Guardar cambios
            usuarioRepository.save(usuario);
            return true;
        }
        
        return false; // El token no existe o ya fue usado
    }
}