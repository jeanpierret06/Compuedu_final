package com.proyecto.AccesoUsuarios.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.services.UsuarioService;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final UsuarioService usuarioService;

    PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // 1. Mostrar el formulario con los datos actuales
    @GetMapping
    public String verPerfil(Authentication auth, Model model) {
        String username = auth.getName();
        // Buscamos al usuario logueado usando el método que ya tienes en el repo/service
        // Nota: Si tu servicio no tiene 'buscarPorUsername', usa el repo o crea el método en el service.
        // Asumo que en tu service puedes llamar al repo.findByUserName
        // Aquí usaremos una lógica genérica asumiendo que tienes acceso al repo o service.
        
        // TRUCO: Como en tu service tienes 'filtrarUsuarios' pero no un 'buscarPorUsername' explícito público,
        // vamos a usar el filtro para encontrarlo rápido (o idealmente agrega findByUsername en tu service).
        Usuario usuario = usuarioService.filtrarUsuarios(username, null).get(0);

        model.addAttribute("usuario", usuario);
        return "perfil"; // La vista que crearemos abajo
    }

    // 2. Procesar la actualización
    @PostMapping("/actualizar")
    public String actualizarPerfil(@ModelAttribute Usuario usuarioForm, Authentication auth, RedirectAttributes redirectAttrs) {
        String username = auth.getName();
        
        // IMPORTANTE: Buscamos el usuario REAL de la base de datos
        Usuario usuarioActual = usuarioService.filtrarUsuarios(username, null).get(0);

        // Solo actualizamos los campos permitidos (Email y Teléfono)
        // NO tocamos password, ni rol, ni id, ni habilitado.
        usuarioActual.setEmail(usuarioForm.getEmail());
        usuarioActual.setDocumento(usuarioForm.getDocumento());

        // Guardamos los cambios
        usuarioService.save(usuarioActual);

        // Mensaje flash para que aparezca en el home
        redirectAttrs.addFlashAttribute("mensaje", "¡Perfil actualizado con éxito!");

        return "redirect:/home"; // O return "redirect:/perfil?exito";
    }
}