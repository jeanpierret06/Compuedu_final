package com.proyecto.AccesoUsuarios.services; // <-- Corregido a plural 'services'

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppService {

    // Instancia de prueba (Reemplaza con tus credenciales reales cuando pases a producción)
    private final String INSTANCE_ID = "tu_instance_id_aqui";
    private final String TOKEN = "tu_token_aqui";
    private final String API_URL = "https://api.ultramsg.com/" + INSTANCE_ID + "/messages/chat";

    public void enviarMensajeAprobado(String telefonoDestino, String nombreCompleto) {
        // Ejecutamos en un hilo asíncrono para que el panel administrativo no sufra demoras de red
        new Thread(() -> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                // Plantilla de mensaje con formato enriquecido para WhatsApp
                String textoMensaje = "¡Hola, " + nombreCompleto + "! 🎓🌟\n\n"
                        + "Nos complace informarte que tus documentos han sido verificados con éxito.\n"
                        + "*Tu cuenta en Compuedu ha sido aprobada y ya se encuentra activa.*\n\n"
                        + "Ya puedes iniciar sesión en la plataforma utilizando tus credenciales.\n\n"
                        + "¡Bienvenido a nuestra comunidad académica!";

                // CORRECCIÓN CRÍTICA: Usamos .put() en lugar de .add()
                Map<String, Object> body = new HashMap<>();
                body.put("token", TOKEN);
                body.put("to", telefonoDestino); // Ej: 573101234567
                body.put("body", textoMensaje);

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
                restTemplate.postForEntity(API_URL, request, String.class);
                
                System.out.println(" Notificación de WhatsApp enviada exitosamente a " + telefonoDestino);

            } catch (Exception e) {
                System.err.println("❌ Error crítico al despachar mensaje de WhatsApp: " + e.getMessage());
            }
        }).start();
    }
}