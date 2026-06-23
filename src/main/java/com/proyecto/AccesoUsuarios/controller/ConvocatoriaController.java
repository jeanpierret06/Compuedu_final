package com.proyecto.AccesoUsuarios.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lowagie.text.DocumentException;
import com.proyecto.AccesoUsuarios.Utils.ComprobanteExporterPDF;
import com.proyecto.AccesoUsuarios.model.Convocatoria;
import com.proyecto.AccesoUsuarios.model.FiltroEstudiante;
import com.proyecto.AccesoUsuarios.model.Inscripcion;
import com.proyecto.AccesoUsuarios.model.Notificacion;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.ConvocatoriaRepository;
import com.proyecto.AccesoUsuarios.repository.FiltroEstudianteRepository;
import com.proyecto.AccesoUsuarios.repository.InscripcionRepository;
import com.proyecto.AccesoUsuarios.repository.LogRepository;
import com.proyecto.AccesoUsuarios.repository.NotificacionRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.services.ConvocatoriaService;
import com.proyecto.AccesoUsuarios.model.LogSystem;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/convocatorias")
@RequiredArgsConstructor 
public class ConvocatoriaController {

    // SE ELIMINARON LOS @AUTOWIRED Y SE AGREGÓ 'private final' A CADA DEPENDENCIA
    private final ConvocatoriaRepository convocatoriaRepo;
    private final InscripcionRepository inscripcionRepo;
    private final UsuarioRepository usuarioRepo;
    private final ConvocatoriaService convocatoriaService;
    private final FiltroEstudianteRepository filtroRepo;
    private final NotificacionRepository notificacionRepo;
    private final LogRepository logRepo; 

    // SE ELIMINÓ EL CONSTRUCTOR MANUAL REDUNDANTE QUE CAUSABA EL CONFLICTO

    // --- SECCION ADMIN ---
    @GetMapping("/nueva")
    public String nuevaConvocatoria(Model model) {
        model.addAttribute("convocatoria", new Convocatoria());
        return "convocatorias/form_admin"; 
    }

    @PostMapping("/guardar") 
    public String guardarConvocatoriaUnificada(
            @ModelAttribute Convocatoria convocatoria, 
            @RequestParam(value = "imagenLocal", required = false) MultipartFile archivo,
            @RequestParam(value = "imagenUrlInput", required = false) String urlExterna,
            @RequestParam(value = "latitud", required = false) Double latitud,
            @RequestParam(value = "longitud", required = false) Double longitud,
            Authentication auth) {
        try {
            String username = auth.getName();
            Usuario creador = usuarioRepo.findByUserName(username).orElseThrow();
            
            convocatoria.setCreador(creador);
            convocatoria.setActiva(true); 
            convocatoria.setEstado("ACTIVA");
            
            if (latitud != null && longitud != null) {
                convocatoria.setLatitud(latitud);
                convocatoria.setLongitud(longitud);
            }

            if (archivo != null && !archivo.isEmpty()) {
                String carpetaUploads = "uploads";
                Path directorioPath = Paths.get(carpetaUploads);
                
                if (!Files.exists(directorioPath)) {
                    Files.createDirectories(directorioPath);
                }
                
                String nombreUnico = UUID.randomUUID().toString() + "_" + archivo.getOriginalFilename();
                Path rutaAbsoluta = directorioPath.resolve(nombreUnico);
                
                Files.write(rutaAbsoluta, archivo.getBytes());
                convocatoria.setImagenUrl("/uploads/" + nombreUnico);
                
            } else if (urlExterna != null && !urlExterna.trim().isEmpty()) {
                convocatoria.setImagenUrl(urlExterna.trim());
            } else {
                convocatoria.setImagenUrl("/images/default-convocatoria.jpg");
            }

            convocatoriaRepo.save(convocatoria);
            System.out.println(">>> Convocatoria guardada por: " + creador.getUserName());

            String detalleExito = "Publicó la convocatoria: '" + convocatoria.getTitulo() + "' (Categoría: " + convocatoria.getCategoria() + ")";
            LogSystem logExito = new LogSystem("CREACIÓN", detalleExito, creador.getUserName(), "SUCCESS");
            logRepo.save(logExito);

            List<FiltroEstudiante> filtros = filtroRepo.findAll();
            String tituloNormalizado = normalizarTexto(convocatoria.getTitulo());

            for (FiltroEstudiante filtro : filtros) {
                if (convocatoria.getCategoria() != null && 
                    convocatoria.getCategoria().equalsIgnoreCase(filtro.getCategoria())) {
                    
                    String palabrasFiltro = normalizarTexto(filtro.getPalabraClave());
                    
                    if (tituloNormalizado.contains(palabrasFiltro)) {
                        Notificacion n = new Notificacion();
                        n.setMensaje("¡Nueva oportunidad para ti! " + convocatoria.getTitulo());
                        n.setUsuario(filtro.getUsuario());
                        n.setConvocatoria(convocatoria);
                        n.setLeida(false);
                        n.setFechaCreacion(LocalDateTime.now());
                        
                        notificacionRepo.save(n);
                        System.out.println(">>> NOTIFICACIÓN CREADA para: " + filtro.getUsuario().getUserName());
                    }
                }
            }

            if ("INSTITUCION".equals(creador.getRol())) {
                return "redirect:/institucion/mis-convocatorias";
            }
            return "redirect:/dashboard";

        } catch (Exception e) {
            System.err.println(">>> ERROR EN GUARDAR: " + e.getMessage());
            e.printStackTrace();

            try {
                String usuarioFallo = (auth != null) ? auth.getName() : "Sistema/Anónimo";
                String detalleError = "Fallo al intentar registrar convocatoria: " + e.getMessage();
                if (detalleError.length() > 255) detalleError = detalleError.substring(0, 250) + "...";
                
                LogSystem logError = new LogSystem("ERROR", detalleError, usuarioFallo, "DANGER");
                logRepo.save(logError);
            } catch (Exception ex) {
                System.err.println("No se pudo escribir el log de error: " + ex.getMessage());
            }
            
            return "redirect:/dashboard?error";
        }
    }

    @GetMapping("/notificaciones/leer/{id}")
    public String marcarComoLeida(@PathVariable Long id) {
        Notificacion noti = notificacionRepo.findById(id).orElseThrow();
        noti.setLeida(true);
        notificacionRepo.save(noti);
        return "redirect:/convocatorias/disponibles";
    }

    @GetMapping("/disponibles")
    public String listarDisponibles(Model model, Authentication auth) {
        List<Convocatoria> convocatorias = convocatoriaRepo.findByEstado("ACTIVA");
        
        String username = auth.getName();
        Usuario usuario = usuarioRepo.findByUserName(username).orElseThrow();
        List<Long> inscripcionesIds = inscripcionRepo.findByUsuario(usuario).stream()
                .map(i -> i.getConvocatoria().getId())
                .collect(Collectors.toList());

        model.addAttribute("convocatorias", convocatorias);
        model.addAttribute("inscripcionesIds", inscripcionesIds);
        
        return "convocatorias/lista_usuario";
    }

    @GetMapping("/detalle/{id}")
    public String verDetallesConvocatoria(@PathVariable("id") Long id, Model model) {
        Convocatoria convocatoria = convocatoriaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Convocatoria no encontrada"));
        
        long totalInscritos = inscripcionRepo.findAll().stream()
                .filter(ins -> ins.getConvocatoria() != null && ins.getConvocatoria().getId().equals(id))
                .count();

        model.addAttribute("convocatoria", convocatoria);
        model.addAttribute("totalInscritos", totalInscritos);
        
        return "convocatorias/detalles"; 
    }

    @PostMapping("/inscribirse/{id}")
    public String inscribirse(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttrs) {
        String username = auth.getName();
        Usuario usuario = usuarioRepo.findByUserName(username).orElseThrow();
        Convocatoria conv = convocatoriaRepo.findById(id).orElseThrow();

        if (inscripcionRepo.existsByUsuarioAndConvocatoria(usuario, conv)) {
            logRepo.save(new LogSystem("INSCRIPCIÓN", "El usuario " + username + " intentó reinscribirse a la convocatoria ID: #" + id, username, "WARNING"));
            redirectAttrs.addFlashAttribute("error", "Ya estabas inscrito en esta convocatoria.");
            return "redirect:/convocatorias/mis-inscripciones";
        }

        if (conv.getVacantes() <= 0) {
            logRepo.save(new LogSystem("INSCRIPCIÓN", "Intento de inscripción fallido por falta de vacantes para la convocatoria ID: #" + id, username, "WARNING"));
            redirectAttrs.addFlashAttribute("error", "Lo sentimos, los cupos para esta convocatoria se acaban de agotar.");
            return "redirect:/convocatorias/mis-inscripciones"; 
        }

        Inscripcion ins = new Inscripcion();
        ins.setUsuario(usuario);
        ins.setConvocatoria(conv);
        ins.setFechaInscripcion(LocalDateTime.now());
        inscripcionRepo.save(ins);
        
        conv.setVacantes(conv.getVacantes() - 1);
        convocatoriaRepo.save(conv);
        
        String detalleLog = "Se inscribió exitosamente a la carrera/convocatoria: '" + conv.getTitulo() + "' (ID: #" + conv.getId() + ")";
        logRepo.save(new LogSystem("INSCRIPCIÓN", detalleLog, username, "SUCCESS"));
        
        redirectAttrs.addFlashAttribute("mensaje", "¡Inscripción exitosa!");
        return "redirect:/convocatorias/mis-inscripciones"; 
    }
    
    @GetMapping("/mis-inscripciones")
    public String misInscripciones(Model model, Authentication auth) {
        String username = auth.getName();
        Usuario usuario = usuarioRepo.findByUserName(username).orElseThrow();
        model.addAttribute("inscripciones", inscripcionRepo.findByUsuario(usuario));
        return "convocatorias/mis_inscripciones";
    }

    @GetMapping("/oportunidades")
    public String verOportunidades(@RequestParam(name = "keyword", required = false) String keyword, 
                                   Authentication auth, 
                                   Model model) {
        
        List<Convocatoria> lista = convocatoriaService.listarParaEstudiante(keyword);
        model.addAttribute("convocatorias", lista);
        model.addAttribute("keyword", keyword);
        
        List<Long> inscripcionesIds = new java.util.ArrayList<>();
        
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            Usuario usuario = usuarioRepo.findByUserName(username).orElseThrow();
            
            List<Inscripcion> misInscripciones = inscripcionRepo.findByUsuario(usuario);
            for (Inscripcion ins : misInscripciones) {
                inscripcionesIds.add(ins.getConvocatoria().getId());
            }
        }
        
        model.addAttribute("inscripcionesIds", inscripcionesIds); 
        return "convocatorias/lista_usuario"; 
    }

    @GetMapping("/comprobante/{id}")
    public void descargarComprobante(@PathVariable Long id, HttpServletResponse response) throws IOException, DocumentException {
        Inscripcion inscripcion = inscripcionRepo.findById(id).orElseThrow();
        
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=comprobante_" + id + ".pdf";
        response.setHeader(headerKey, headerValue);

        ComprobanteExporterPDF exporter = new ComprobanteExporterPDF(inscripcion);
        exporter.exportar(response);
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String normalized = Normalizer.normalize(texto, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").toLowerCase();
    }
}