package com.proyecto.AccesoUsuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AccesoUsuariosApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccesoUsuariosApplication.class, args);
    }

    // El método @Bean de RestTemplate que estaba aquí ha sido removido
    // para eliminar la duplicación con SecurityConfig.
}