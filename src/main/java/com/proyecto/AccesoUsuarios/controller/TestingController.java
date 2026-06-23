package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.Inscripcion;
import com.proyecto.AccesoUsuarios.repository.InscripcionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/testing")
@RequiredArgsConstructor // <-- Automatiza el constructor para los campos privados y finales
public class TestingController {

    // Se elimina el @Autowired y se marca como final para asegurar inmutabilidad
    private final InscripcionRepository inscripcionRepository;

    // Endpoint para que la Institución evalúe (Aceptar o Rechazar)
    @PostMapping("/evaluar/{inscripcionId}") // El nombre aquí...
    public ResponseEntity<String> evaluarPostulacion(
            @PathVariable("inscripcionId") Long dbInscripcionId, // <-- CORREGIDO: Se mapea explícitamente el nombre de la ruta al parámetro
            @RequestParam String nuevoEstado) {
        
        String estadoUpper = nuevoEstado.toUpperCase();
        if (!estadoUpper.equals("ACEPTADO") && !estadoUpper.equals("RECHAZADO")) {
            return ResponseEntity.badRequest().body("Estado inválido. Use ACEPTADO o RECHAZADO.");
        }

        Optional<Inscripcion> inscripcionOpt = inscripcionRepository.findById(dbInscripcionId);
        if (inscripcionOpt.isPresent()) {
            Inscripcion inscripcion = inscripcionOpt.get();
            inscripcion.setEstado(estadoUpper);
            inscripcionRepository.save(inscripcion);
            return ResponseEntity.ok("Postulación actualizada a: " + estadoUpper);
        }
        
        return ResponseEntity.notFound().build();
    }

    // Endpoint para que el estudiante consulte sus estados desde el frontend
    @GetMapping("/mis-postulaciones/{estudianteId}")
    public ResponseEntity<List<Inscripcion>> obtenerPostulaciones(@PathVariable Long estudianteId) {
        // CORRECCIÓN NOTA DE SEGURIDAD: Asegúrate de que en 'InscripcionRepository' el método se llame findByUsuario_Id
        // si es que tu propiedad dentro de la clase Inscripcion se llama 'usuario'.
        return ResponseEntity.ok(inscripcionRepository.findByUsuario_Id(estudianteId));
    }
}