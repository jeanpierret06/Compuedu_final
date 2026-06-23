package com.proyecto.AccesoUsuarios.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.lowagie.text.DocumentException;
import com.proyecto.AccesoUsuarios.model.SoporteDocumento;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.SoporteDocumentoRepository;
import com.proyecto.AccesoUsuarios.services.UsuarioService;
import com.proyecto.AccesoUsuarios.Utils.UsuarioExporterPDF;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor // <-- Genera automáticamente el constructor público con todos los campos declarados como final
public class UsuarioController {

    // Se eliminó la anotación @Autowired y se marcaron todos los campos como private final
    private final SoporteDocumentoRepository soporteDocumentoRepo;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    // ---------------------------------------------------------
    // 1. NAVEGACIÓN PRINCIPAL
    // ---------------------------------------------------------

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    // ---------------------------------------------------------
    // 2. REGISTRO Y GESTIÓN
    // ---------------------------------------------------------

    @GetMapping("/registro")
    public String registroForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "form"; 
    }

    @PostMapping("/registro")
    public String registrarUsuario(@ModelAttribute Usuario usuario,
            @RequestParam("archivoAdjunto") MultipartFile documento,
            RedirectAttributes redirectAttributes) {

        if (usuarioService.existsByUserName(usuario.getUserName())) {
            redirectAttributes.addFlashAttribute("error",
                    "El nombre de usuario '" + usuario.getUserName() + "' ya existe.");
            return "redirect:/registro";
        }

        if (documento.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "Debe adjuntar obligatoriamente el documento de verificación.");
            return "redirect:/registro";
        }

        try {
            SoporteDocumento soporteMongo = new SoporteDocumento();

            soporteMongo.setNombreOriginal(documento.getOriginalFilename());
            soporteMongo.setTipoMime(documento.getContentType());
            soporteMongo.setArchivoBinario(documento.getBytes());
            soporteMongo.setFechaSubida(new Date());

            SoporteDocumento guardado = soporteDocumentoRepo.save(soporteMongo);

            usuario.setDocumentoUrl(guardado.getId());
            usuario.setEstado("PENDIENTE");
            usuario.setHabilitado(false);

            usuarioService.save(usuario);

            guardado.setIdEstudiante(usuario.getId());
            soporteDocumentoRepo.save(guardado);

            redirectAttributes.addFlashAttribute("exito",
                    "¡Tu solicitud ha sido enviada! Tus documentos están en proceso de verificación por el administrador. Serás notificado a la brevedad.");
            return "redirect:/login";

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Fallo de lectura de datos binarios: " + e.getMessage());
            return "redirect:/registro";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar: " + e.getMessage());
            return "redirect:/registro";
        }
    }

    @GetMapping("/usuarios/nuevo")
    public String crearUsuarioAdmin(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "form"; 
    }

    @PostMapping("/guardarUsuario")
    public String guardarUsuario(@ModelAttribute Usuario usuario, Authentication auth) {

        if (usuario.getId() != null) {
            Usuario usuarioExistente = usuarioService.obtenerPorId(usuario.getId());
            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                usuario.setPassword(usuarioExistente.getPassword());
            } else {
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            }
        } else {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }

        usuario.setHabilitado(true);

        if ("ADMIN".equals(usuario.getRol())) {
            if (auth == null || !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                usuario.setRol("USER");
            }
        }

        if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
            usuario.setRol("USER");
        }

        usuarioService.save(usuario);

        if (auth != null && auth.isAuthenticated()) {
            return "redirect:/usuarios";
        }

        return "redirect:/login?registrado";
    }

    // ---------------------------------------------------------
    // 3. GESTIÓN DE USUARIOS (ADMIN) - CON FILTROS
    // ---------------------------------------------------------

    @GetMapping("/usuarios")
    public String listarUsuarios(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "rol", required = false) String rol,
            Model model) {

        String k = (keyword != null && !keyword.trim().isEmpty()) ? keyword : null;
        String r = (rol != null && !rol.trim().isEmpty() && !rol.equals("Todos")) ? rol : null;

        List<Usuario> listaUsuarios = usuarioService.filtrarUsuarios(k, r);

        model.addAttribute("usuarios", listaUsuarios);
        model.addAttribute("keyword", keyword);
        model.addAttribute("rol", rol);

        return "usuarios";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        usuario.setPassword("");
        model.addAttribute("usuario", usuario);
        return "editar";
    }

    @PostMapping("/usuarios/actualizar")
    public String actualizarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.save(usuario); 
            redirectAttributes.addFlashAttribute("exito", "Usuario actualizado correctamente");
            return "redirect:/usuarios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
            return "redirect:/usuarios/editar/" + usuario.getId();
        }
    }

    @GetMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable("id") Long idParametro,
            @AuthenticationPrincipal UserDetails usuarioLogueado,
            RedirectAttributes flash) {

        Usuario adminEnSesion = usuarioService.findByUserName(usuarioLogueado.getUsername());

        if (adminEnSesion.getId().equals(idParametro)) {
            flash.addFlashAttribute("error", "No puedes eliminar tu propia cuenta mientras estás en sesión.");
            return "redirect:/usuarios";
        }

        try {
            usuarioService.eliminar(idParametro);
            flash.addFlashAttribute("success", "Usuario eliminado correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo eliminar el usuario.");
        }

        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/estado/{id}")
    public String cambiarEstadoUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        usuario.setHabilitado(!usuario.isHabilitado());
        usuarioService.save(usuario);
        return "redirect:/usuarios";
    }

    // ---------------------------------------------------------
    // 4. EXPORTAR A PDF
    // ---------------------------------------------------------

    @GetMapping("/usuarios/exportarPDF")
    public void exportarListadoDeUsuariosEnPDF(@RequestParam(name = "keyword", required = false) String keyword,
            HttpServletResponse response) throws IOException, DocumentException {

        response.setContentType("application/pdf");

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=usuarios_" + currentDateTime + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Usuario> listaUsuarios;

        if (keyword != null && !keyword.isEmpty()) {
            listaUsuarios = usuarioService.listarUsuarios(keyword);
        } else {
            listaUsuarios = usuarioService.listarUsuarios();
        }

        UsuarioExporterPDF exporter = new UsuarioExporterPDF(listaUsuarios);
        exporter.exportar(response);
    }
}