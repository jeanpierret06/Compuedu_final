package com.proyecto.AccesoUsuarios.controller;

import org.springframework.security.core.Authentication; 
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.proyecto.AccesoUsuarios.model.FiltroEstudiante;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.FiltroEstudianteRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/estudiante")
@RequiredArgsConstructor // <-- Genera el constructor automático en tiempo de compilación para los campos 'final'
public class EstudianteController {

    // Se removieron las anotaciones @Autowired y se añadieron los modificadores 'private final'
    private final FiltroEstudianteRepository filtroRepo;
    private final UsuarioRepository usuarioRepo;

    @PostMapping("/guardar-alerta")
    public String guardarAlerta(@RequestParam String palabraClave, 
                                @RequestParam String categoria, 
                                Authentication auth) {
        
        // Obtenemos el nombre del usuario logueado actualmente
        String username = auth.getName();
        
        // Buscamos el objeto Usuario completo en la DB
        Usuario usuario = usuarioRepo.findByUserName(username)
                            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Creamos la nueva alerta vinculada a ese usuario
        FiltroEstudiante nuevoFiltro = new FiltroEstudiante();
        nuevoFiltro.setPalabraClave(palabraClave);
        nuevoFiltro.setCategoria(categoria);
        nuevoFiltro.setUsuario(usuario);

        // Guardamos en la tabla filtros_estudiante
        filtroRepo.save(nuevoFiltro);

        return "redirect:/dashboard?alertaGuardada=true";
    }
}