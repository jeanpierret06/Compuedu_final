package com.proyecto.AccesoUsuarios.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository repo) {

        return args -> {
            if (repo.findByUserName("manny").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setUserName("manny");
                admin.setPassword(new BCryptPasswordEncoder().encode("06092006"));
                admin.setNombre("Administrador");
                admin.setApellido("Del Sistema"); // ANTES ESTABA VACÍO O NO EXISTÍA
                admin.setEmail("admin@compuedu.com"); // NUEVO OBLIGATORIO
                admin.setDocumento("000000000");      // NUEVO OBLIGATORIO
                admin.setRol("ADMIN");
                admin.setTelefono("123456789");
                admin.setHabilitado(true);
                // --- ESTA LÍNEA ES VITAL ---
                // Si no la pones, el admin nace deshabilitado y nadie puede entrar al sistema
                admin.setHabilitado(true); 
                // ---------------------------

                repo.save(admin);
                System.out.println("✅ Usuario admin creado con éxito y habilitado.");
            } 
            else {
                System.out.println("ℹ️ Usuario admin ya existe");
            }
        };
    }
}