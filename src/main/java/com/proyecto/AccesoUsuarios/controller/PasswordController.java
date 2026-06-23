package com.proyecto.AccesoUsuarios.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyecto.AccesoUsuarios.services.PassswordServices;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/auth") // Agrupa y organiza de forma limpia todas las rutas de autenticación
@RequiredArgsConstructor // <-- Automatiza el constructor público para la dependencia final
public class PasswordController {

    private final PassswordServices passwordServices;

    // Muestra el formulario HTML de recuperación base
    @GetMapping("/recuperar")
    public String mostrarFormulario() {
        return "recuperar_password"; 
    }

    // Ruta que se abre al hacer clic en el enlace del correo electrónico enviado por Python
    @GetMapping("/reset-password")
    public String mostrarFormularioCambio(@RequestParam("token") String token, Model model) {
        // Pasamos el token al HTML para identificar la sesión de restablecimiento
        model.addAttribute("token", token);
        return "nueva_password"; 
    }

    @PostMapping("/actualizar-password")
    public String actualizarPassword(@RequestParam("token") String token, 
                                     @RequestParam("password") String password, 
                                     Model model) {
        boolean exito = passwordServices.cambiarPasswordConToken(token, password);
        
        if (exito) {
            return "redirect:/login?resetSuccess";
        } else {
            model.addAttribute("error", "El enlace es inválido o ha expirado.");
            model.addAttribute("token", token); // Volvemos a inyectar el token para no romper el formulario
            return "nueva_password";
        }
    }

    // MODIFICACIÓN CRÍTICA: Se añade RedirectAttributes para aplicar patrón POST-REDIRECT-GET
    @PostMapping("/enviar-solicitud")
    public String procesarRecuperacion(@RequestParam("email") String email, RedirectAttributes flash) {
        try {
            passwordServices.solicitarRestablecimiento(email);
            // El mensaje sobrevive a la redirección y evita reenvíos de correo al actualizar con F5
            flash.addFlashAttribute("mensaje", "Si el correo existe en nuestra plataforma, se enviará un enlace en breve.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error interno al procesar la solicitud de recuperación.");
        }
        return "redirect:/auth/recuperar"; 
    }
}