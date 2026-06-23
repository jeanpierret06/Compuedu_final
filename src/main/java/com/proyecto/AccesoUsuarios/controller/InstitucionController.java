package com.proyecto.AccesoUsuarios.controller;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyecto.AccesoUsuarios.model.Convocatoria;
import com.proyecto.AccesoUsuarios.model.Inscripcion;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.ConvocatoriaRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.repository.InscripcionRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/institucion")
@RequiredArgsConstructor // <-- Automatiza el constructor para los campos privados y finales
public class InstitucionController {

    // Se eliminan los @Autowired individuales y se declaran como variables finales
    // inmutables
    private final UsuarioRepository usuarioRepo;
    private final ConvocatoriaRepository convocatoriaRepo;
    private final InscripcionRepository inscripcionRepo;

    // [NUEVO] Inyección dinámica para la URL de la API de Python en Render (evita
    // el localhost en la nube)
    @Value("${app.url.python:http://localhost:5000}")
    private String urlBasePython;

    @Value("${app.url.estadisticas:http://localhost/PHP/institucion/estadisticas.php}")
    private String urlEstadisticasPHP;

    @GetMapping("/estadisticas")
    public String verEstadisticas() {
        // Redirige exclusivamente al microservicio de estadísticas institucionales
        return "redirect:" + urlEstadisticasPHP;
    }

    @GetMapping("/dashboard")
    public String dashboardInstitucion(Authentication auth, Model model) {
        try {
            // 1. Identificación dinámica del usuario
            String username = auth.getName();
            Usuario institucion = usuarioRepo.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Long idDinamico = institucion.getId();
            System.out.println("========== DASHBOARD START ==========");
            System.out.println(">>> Usuario: " + username + " | ID: " + idDinamico);

            // 2. Datos locales (Java)
            model.addAttribute("nombreUsuario", username);
            model.addAttribute("misConvocatoriasCount", convocatoriaRepo.countByCreador(institucion));
            model.addAttribute("misInscritosCount", inscripcionRepo.countByConvocatoria_Creador(institucion));
            model.addAttribute("ultimosInscritos", inscripcionRepo.findByConvocatoria_Creador(institucion));

            // 3. Llamada al Microservicio parametrizado externamente sin Hardcoding
            String urlPython = urlBasePython + "/api/stats/institucion/" + idDinamico;
            System.out.println(">>> Llamando a la API Analítica: " + urlPython);

            try {
                RestTemplate restTemplate = new RestTemplate();
                // Corrección de Raw Type a Mapa Parameterizado
                @SuppressWarnings("unchecked")
                Map<String, Object> stats = restTemplate.getForObject(urlPython, Map.class);

                if (stats != null && !stats.isEmpty()) {
                    System.out.println(">>> STATS RECIBIDAS: " + stats);
                    model.addAttribute("stats", stats);
                } else {
                    System.out.println(">>> ADVERTENCIA: Python devolvió datos vacíos.");
                    model.addAttribute("stats", new HashMap<String, Object>());
                }
            } catch (Exception e) {
                System.out.println(">>> ERROR PYTHON: " + e.getMessage());
                model.addAttribute("error", "Servicio analítico no disponible");
                model.addAttribute("stats", null);
            }

            System.out.println("========== DASHBOARD END ==========");

        } catch (Exception e) {
            System.err.println(">>> ERROR GENERAL EN DASHBOARD: " + e.getMessage());
            e.printStackTrace();
        }

        return "dashboard/institucion";
    }

    @PostMapping("/inscripciones/cambiar-estado")
    public String cambiarEstadoInscripcion(@RequestParam("id") Long id,
            @RequestParam("estado") String nuevoEstado,
            RedirectAttributes redirectAttributes,
            Authentication auth) {
        try {
            String username = auth.getName();
            Usuario institucion = usuarioRepo.findByUserName(username).orElseThrow();

            Inscripcion inscripcion = inscripcionRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

            if (!inscripcion.getConvocatoria().getCreador().getId().equals(institucion.getId())) {
                redirectAttributes.addFlashAttribute("mensajeError",
                        "No tienes autorización para modificar esta postulación.");
                return "redirect:/institucion/dashboard";
            }

            inscripcion.setEstado(nuevoEstado);
            inscripcionRepo.save(inscripcion);

            String mensaje = nuevoEstado.equals("ACEPTADO") ? "¡Estudiante aceptado correctamente!"
                    : "Postulación rechazada.";
            redirectAttributes.addFlashAttribute("mensajeExito", mensaje);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al procesar la solicitud: " + e.getMessage());
        }

        return "redirect:/institucion/dashboard";
    }

    @GetMapping("/mis-convocatorias")
    public String listarMisConvocatorias(Model model, Authentication auth) {
        String username = auth.getName();
        Usuario institucion = usuarioRepo.findByUserName(username).orElseThrow();

        model.addAttribute("convocatorias", convocatoriaRepo.findByCreador(institucion));
        model.addAttribute("nombreUsuario", institucion.getUserName());

        return "institucion/mis_convocatorias";
    }

    @PostMapping("/convocatorias/toggle/{id}")
    public String toggleEstadoConvocatoria(@PathVariable Long id, Authentication auth) {
        String username = auth.getName();
        Usuario institucion = usuarioRepo.findByUserName(username).orElseThrow();

        Convocatoria conv = convocatoriaRepo.findById(id)
                .filter(c -> c.getCreador().getId().equals(institucion.getId()))
                .orElseThrow(() -> new RuntimeException("No autorizado"));

        if (!"ANULADA".equals(conv.getEstado())) {
            if ("ACTIVA".equals(conv.getEstado())) {
                conv.setEstado("INACTIVA");
                conv.setActiva(false);
            } else {
                conv.setEstado("ACTIVA");
                conv.setActiva(true);
            }
            declararEstado(conv);
        }

        return "redirect:/institucion/mis-convocatorias";
    }

    private void declararEstado(Convocatoria conv) {
        convocatoriaRepo.save(conv);
    }

    @GetMapping("/inscripciones/listado")
    public String listarPostulantesMóduloAvanzado(
            @RequestParam(name = "estado", required = false, defaultValue = "TODOS") String estado,
            Model model) {

        List<Inscripcion> todas = inscripcionRepo.findAll();

        List<Inscripcion> filtradas;
        if ("TODOS".equalsIgnoreCase(estado)) {
            filtradas = todas;
        } else {
            filtradas = todas.stream()
                    .filter(ins -> ins.getEstado() != null && ins.getEstado().equalsIgnoreCase(estado))
                    .collect(Collectors.toList());
        }

        long totalCount = todas.size();
        long aceptadosCount = todas.stream().filter(ins -> "ACEPTADO".equalsIgnoreCase(ins.getEstado())).count();
        long rechazadosCount = todas.stream().filter(ins -> "RECHAZADO".equalsIgnoreCase(ins.getEstado())).count();
        long pendientesCount = todas.stream()
                .filter(ins -> ins.getEstado() == null || "PENDIENTE".equalsIgnoreCase(ins.getEstado())).count();

        model.addAttribute("inscripciones", filtradas);
        model.addAttribute("estadoActivo", estado.toUpperCase());
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("aceptadosCount", aceptadosCount);
        model.addAttribute("rechazadosCount", rechazadosCount);
        model.addAttribute("pendientesCount", pendientesCount);

        return "institucion/listado";
    }

    @GetMapping("/reportes/exportar")
    public ResponseEntity<byte[]> exportarReporteInteligente(
            @RequestParam(name = "estado", defaultValue = "TODOS") String estado) {

        List<Inscripcion> todas = inscripcionRepo.findAll();

        List<Inscripcion> filtradas;
        if ("TODOS".equalsIgnoreCase(estado)) {
            filtradas = todas;
        } else {
            filtradas = todas.stream()
                    .filter(ins -> ins.getEstado() != null && ins.getEstado().equalsIgnoreCase(estado))
                    .collect(Collectors.toList());
        }

        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("\uFEFF");
        csvBuilder.append("Estudiante;Email;Telefono;Convocatoria;Fecha Inscripcion;Estado Actual\n");

        for (Inscripcion ins : filtradas) {
            String nombre = (ins.getUsuario() != null) ? ins.getUsuario().getUserName() : "N/A";
            String email = (ins.getUsuario() != null) ? ins.getUsuario().getEmail() : "N/A";
            String telefono = (ins.getUsuario() != null) ? ins.getUsuario().getTelefono() : "N/A";
            String convocatoria = (ins.getConvocatoria() != null) ? ins.getConvocatoria().getTitulo() : "N/A";
            String fecha = (ins.getFechaInscripcion() != null) ? ins.getFechaInscripcion().toString() : "N/A";
            String est = (ins.getEstado() != null) ? ins.getEstado() : "PENDIENTE";

            csvBuilder.append(nombre).append(";")
                    .append(email).append(";")
                    .append(telefono).append(";")
                    .append(convocatoria).append(";")
                    .append(fecha).append(";")
                    .append(est).append("\n");
        }

        byte[] csvBytes = csvBuilder.toString().getBytes(StandardCharsets.UTF_8);
        String nombreArchivo = "Reporte_Postulantes_" + estado.toUpperCase() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "must-revalidate, post-check=0, pre-check=0")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }
}