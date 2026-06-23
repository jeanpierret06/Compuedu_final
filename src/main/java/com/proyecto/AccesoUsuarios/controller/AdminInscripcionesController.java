package com.proyecto.AccesoUsuarios.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.proyecto.AccesoUsuarios.repository.InscripcionRepository;

@Controller
@RequestMapping("/admin/inscripciones")
public class AdminInscripcionesController {

    private final InscripcionRepository inscripcionRepo;

    AdminInscripcionesController(InscripcionRepository inscripcionRepo) {
        this.inscripcionRepo = inscripcionRepo;
    }

    // 1. Listar todas las inscripciones
    @GetMapping
    public String listarInscripciones(Model model) {
        // Obtenemos todas las inscripciones de la base de datos
        // (Aquí podrías agregar filtros más adelante si quisieras)
        model.addAttribute("inscripciones", inscripcionRepo.findAll());
        
        return "admin/lista_inscripciones"; // La vista HTML que crearemos abajo
    }

    // 2. Eliminar una inscripción (Por si hubo un error)
    @GetMapping("/eliminar/{id}")
    public String eliminarInscripcion(@PathVariable Long id) {
        inscripcionRepo.deleteById(id);
        return "redirect:/admin/inscripciones";
    }
}