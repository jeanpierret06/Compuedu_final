package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.SoporteDocumento;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.LogRepository;
import com.proyecto.AccesoUsuarios.repository.SoporteDocumentoRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.services.WhatsAppService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminLogController {

    // Atributos finales inmutables
    private final LogRepository logRepository;
    private final WhatsAppService whatsAppService;
    private final UsuarioRepository usuarioRepo;
    private final SoporteDocumentoRepository soporteDocumentoRepo;

    // SOLUCIÓN DEFINITIVA A WARNINGS: Inyección unificada de todas las dependencias
    // por constructor
    public AdminLogController(LogRepository logRepository,
            WhatsAppService whatsAppService,
            UsuarioRepository usuarioRepo,
            SoporteDocumentoRepository soporteDocumentoRepo) {
        this.logRepository = logRepository;
        this.whatsAppService = whatsAppService;
        this.usuarioRepo = usuarioRepo;
        this.soporteDocumentoRepo = soporteDocumentoRepo;
    }

    // VISTA DE LA TABLA DE CONTROL DE DOCUMENTOS PENDIENTES
    @GetMapping("/verificar-usuarios")
    public String listarUsuariosPendientes(Model model) {
        List<Usuario> listaPendientes = usuarioRepo.findByEstado("PENDIENTE");
        model.addAttribute("usuarios", listaPendientes);
        return "admin/verificar-usuarios";
    }

    // PROCESO DE APROBACIÓN
    @PostMapping("/usuarios/aprobar/{id}")
    public String aprobarUsuario(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioRepo.findById(id).orElse(null);

            if (usuario != null) {
                usuario.setEstado("APROBADO");
                usuario.setHabilitado(true);
                usuarioRepo.save(usuario);

                if (usuario.getTelefono() != null && !usuario.getTelefono().trim().isEmpty()) {
                    String nombreCompleto = usuario.getNombre() + " " + usuario.getApellido();
                    if ("INSTITUCION".equals(usuario.getRol())) {
                        nombreCompleto = usuario.getNombreInstitucion();
                    }
                    whatsAppService.enviarMensajeAprobado(usuario.getTelefono(), nombreCompleto);
                }

                redirectAttributes.addFlashAttribute("exito", "El usuario '" + usuario.getUserName()
                        + "' ha sido aprobado y se ha despachado la notificación.");
            } else {
                redirectAttributes.addFlashAttribute("error", "El usuario con ID " + id + " no fue localizado.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error sistémico al aprobar: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    // PROCESO DE RECHAZO
    @PostMapping("/usuarios/rechazar/{id}")
    public String rechazarUsuario(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioRepo.findById(id).orElse(null);

            if (usuario != null) {
                usuario.setEstado("RECHAZADO");
                usuario.setHabilitado(false);
                usuarioRepo.save(usuario);

                redirectAttributes.addFlashAttribute("exito",
                        "La solicitud de '" + usuario.getUserName() + "' ha sido rechazada.");
            } else {
                redirectAttributes.addFlashAttribute("error", "El usuario con ID " + id + " no fue localizado.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error sistémico al rechazar: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    // VISUALIZACIÓN EN TIEMPO REAL DEL ARCHIVO DE MONGO ATLAS (PDF/IMAGEN)
    // URL Real expuesta: /admin/log/ver-archivo/{id}
    @GetMapping("/log/ver-archivo/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> verArchivoDesdeLog(@PathVariable("id") String id) {
        Optional<SoporteDocumento> soporteOptional = soporteDocumentoRepo.findById(id);

        if (soporteOptional.isPresent()) {
            SoporteDocumento archivo = soporteOptional.get();

            if (archivo.getArchivoBinario() != null) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=\"" + archivo.getNombreOriginal() + "\"")
                        .contentType(MediaType.parseMediaType(archivo.getTipoMime()))
                        .body(archivo.getArchivoBinario());
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/logs")
    public String verLogsSistema(Model model) {
        model.addAttribute("logs", logRepository.findAllByOrderByFechaHoraDesc());
        return "admin/logs";
    }

    @GetMapping("/analiticas")
        public String verAnaliticasAdmin(HttpServletRequest request) {
            String serverName = request.getServerName();
            
            if (serverName.equals("localhost")) {
                // LOCAL: Redirige al XAMPP de tu computadora
                return "redirect:http://localhost/PHP/admin/analiticas-admin.php";
            } else {
                // NUBE: Redirige a tu nuevo servicio Docker de PHP en Render
                return "redirect:https://php-8o2x.onrender.com/admin/analiticas-admin.php";
            }
        }
}