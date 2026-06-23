package com.proyecto.AccesoUsuarios.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // <-- Genera automáticamente el constructor público para la dependencia inmutable (final)
public class SecurityConfig {

    // Se eliminó la anotación @Autowired y se transformó en un atributo inmutable
    private final UserDetailsService userDetailsService;

    // =========================================================================
    // MODIFICACIÓN: Registro del cliente HTTP requerido por PassswordServices
    // =========================================================================
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                // 1. Recursos estáticos
                .requestMatchers("/css/**", "/js/**", "/img/**", "/images/**", "/webjars/**").permitAll()
                    
                // 2. Rutas Públicas
                .requestMatchers("/", "/index", "/home", "/login", "/registro", "/guardarUsuario").permitAll()

                // Permite acceso a recuperación de contraseña sin loguearse
                .requestMatchers("/auth/**").permitAll()

                // Rutas que requieren autenticación base
                .requestMatchers("/estudiante/guardar-alerta").authenticated()

                // 3. Rutas EXCLUSIVAS de ADMINISTRADOR (Gestión de usuarios y sistema)
                .requestMatchers("/admin/**", "/usuarios/**").hasRole("ADMIN")

                // 4. Rutas COMPARTIDAS: ADMIN e INSTITUCION (Crear convocatorias)
                // Spring Security valida automáticamente contra "ROLE_ADMIN" y "ROLE_INSTITUCION"
                .requestMatchers("/convocatorias/nueva", "/convocatorias/guardar")
                    .hasAnyRole("ADMIN", "INSTITUCION") 
                
                // 5. Rutas de USUARIO/ESTUDIANTE
                .requestMatchers("/convocatorias/inscribirse/**", "/convocatorias/mis-inscripciones").hasRole("USER")

                // 6. Rutas Comunes (Dashboard, Perfil, PDF, etc.)
                .anyRequest().authenticated()
                
            )
            .formLogin(form -> form
                .loginPage("/login") 
                .defaultSuccessUrl("/dashboard", true) // El DashboardController redirige dinámicamente según el rol detectado
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Reemplaza tu método authenticationProvider() actual por esta estructura:
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);
            
        authenticationManagerBuilder
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
            
        return authenticationManagerBuilder.build();
    }
}