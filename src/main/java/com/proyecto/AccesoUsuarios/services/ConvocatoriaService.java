package com.proyecto.AccesoUsuarios.services;

import com.proyecto.AccesoUsuarios.model.Convocatoria;
import com.proyecto.AccesoUsuarios.repository.ConvocatoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor // <-- Genera automáticamente el constructor para las dependencias final, limpiando el diseño
public class ConvocatoriaService {

    private final ConvocatoriaRepository convocatoriaRepository;
    private final RestTemplate restTemplate; // <-- Inyectado de forma global e inmutable

    // [NUEVO] Inyección de la URL base de Python configurada desde las propiedades de entorno
    @Value("${app.url.python:http://localhost:5000}")
    private String urlBasePython;

    public List<Convocatoria> listarParaEstudiante(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return convocatoriaRepository.buscarPorKeyword(keyword);
        }
        return convocatoriaRepository.findByActivaTrue();
    }

    public Map<String, Object> obtenerEstadisticasPython(Long institucionId) {
        // Se construye la URL usando la propiedad dinámica inyectada
        String url = urlBasePython + "/api/stats/institucion/" + institucionId;
        
        // [CORRECCIÓN CRÍTICA DE VS CODE]: Usamos exchange con ParameterizedTypeReference para definir estrictamente 
        // que el JSON se debe transformar a un Map con llaves String y valores Object, eliminando el warning de Type safety.
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        
        return response.getBody();
    }

    public Convocatoria findById(Long id) {
        return convocatoriaRepository.findById(id).orElse(null);
    }
}