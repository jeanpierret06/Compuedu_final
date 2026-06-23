package com.proyecto.AccesoUsuarios.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyecto.AccesoUsuarios.model.Convocatoria;
import com.proyecto.AccesoUsuarios.repository.ConvocatoriaRepository;

@Controller
@RequestMapping("/admin/gestion-convocatorias")

public class AdminConvocatoriaController {

    private final ConvocatoriaRepository convocatoriaRepo;

    AdminConvocatoriaController(ConvocatoriaRepository convocatoriaRepo) {
        this.convocatoriaRepo = convocatoriaRepo;
    }

    // 1. Ver todas las convocatorias (De todas las instituciones)
    @GetMapping
    public String listarTodas(Model model) {
        model.addAttribute("convocatorias", convocatoriaRepo.findAll());
        return "admin/gestion_convocatorias"; // Vista nueva
    }

    // 2. Acción de ANULAR
    @GetMapping("/anular/{id}")
    public String anularConvocatoria(@PathVariable Long id, RedirectAttributes attrs) {
        Convocatoria conv = convocatoriaRepo.findById(id).orElseThrow();
        
        conv.setEstado("ANULADA"); // Cambiamos el estado
        conv.setActiva(false);     // También bajamos el flag de activa por seguridad
        
        convocatoriaRepo.save(conv);
        
        attrs.addFlashAttribute("mensaje", "La convocatoria ha sido anulada correctamente.");
        return "redirect:/admin/gestion-convocatorias";
    }

    // 3. Acción de REACTIVAR (Por si el admin se equivocó)
    @GetMapping("/reactivar/{id}")
    public String reactivarConvocatoria(@PathVariable Long id, RedirectAttributes attrs) {
        Convocatoria conv = convocatoriaRepo.findById(id).orElseThrow();
        
        conv.setEstado("ACTIVA");
        conv.setActiva(true);
        
        convocatoriaRepo.save(conv);
        
        attrs.addFlashAttribute("mensaje", "La convocatoria ha sido reactivada.");
        return "redirect:/admin/gestion-convocatorias";
    }
}