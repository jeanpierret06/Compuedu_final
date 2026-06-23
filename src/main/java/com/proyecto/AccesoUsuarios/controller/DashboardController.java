package com.proyecto.AccesoUsuarios.controller;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.proyecto.AccesoUsuarios.model.Notificacion;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.ConvocatoriaRepository;
import com.proyecto.AccesoUsuarios.repository.InscripcionRepository;
import com.proyecto.AccesoUsuarios.repository.NotificacionRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.repository.LogRepository; 

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor // <-- Genera automáticamente el constructor para los campos marcados como final
public class DashboardController {

    // Se eliminan los @Autowired individuales y se transforman en dependencias inmutables
    private final NotificacionRepository notificacionRepo;
    private final UsuarioRepository usuarioRepo;
    private final ConvocatoriaRepository convocatoriaRepo;
    private final InscripcionRepository inscripcionRepo;
    private final LogRepository logRepository; 

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        String username = auth.getName();
        Usuario usuario = usuarioRepo.findByUserName(username).orElseThrow(); 

        // -----------------------------------------------------------
        // 1. LÓGICA PARA EL ADMINISTRADOR
        // -----------------------------------------------------------
        if ("ADMIN".equals(usuario.getRol()) || auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            
            // KPIs dinámicos vinculados con las tarjetas de la vista admin
            model.addAttribute("totalUsuarios", usuarioRepo.count());
            model.addAttribute("usuariosPendientes", usuarioRepo.countByEstado("PENDIENTE")); 
            model.addAttribute("convocatoriasActivas", convocatoriaRepo.count());
            model.addAttribute("totalInscripciones", inscripcionRepo.count());
            model.addAttribute("totalLogs", logRepository.count());
            
            return "dashboard/admin"; 
        }
        
        // -----------------------------------------------------------
        // 2. LÓGICA PARA LA INSTITUCIÓN
        // -----------------------------------------------------------
        else if ("INSTITUCION".equals(usuario.getRol()) || auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_INSTITUCION"))) {
            return "redirect:/institucion/dashboard"; 
        }

        // -----------------------------------------------------------
        // 3. LÓGICA PARA EL ESTUDIANTE (USUARIO)
        // -----------------------------------------------------------
        else {
            List<Notificacion> notificaciones = notificacionRepo.findByUsuarioAndLeidaFalse(usuario);
            
            model.addAttribute("notificaciones", notificaciones);
            model.addAttribute("notificacionesNoLeidas", notificaciones.size());
            model.addAttribute("misInscripciones", inscripcionRepo.findByUsuario(usuario));
            model.addAttribute("convocatoriasDisponibles", convocatoriaRepo.findAll());
            model.addAttribute("nombreUsuario", usuario.getUserName());

            return "home"; 
        }
    }
}