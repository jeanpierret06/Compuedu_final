package com.proyecto.AccesoUsuarios.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.Date;

@Document(collection = "soporte_documentos")
public class SoporteDocumento {

    @Id
    private String id;

    @Field("id_estudiante")
    private Long idEstudiante;

    @Field("nombre_original")
    private String nombreOriginal;

    @Field("tipo_mime")
    private String tipoMime; // Reemplaza Base64 por el tipo real del archivo para que el navegador lo dibuje

    @Field("archivo_binario")
    private byte[] archivoBinario; // Guarda eficientemente los bytes puros (BinData en Atlas)

    @Field("fecha_subida")
    private Date fechaSubida;

    // CONSTRUCTOR VACÍO OBLIGATORIO PARA SPRING DATA
    public SoporteDocumento() {
    }

    // GETTERS Y SETTERS EXPLÍCITOS (Solución al error del IDE)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(Long idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public void setNombreOriginal(String nombreOriginal) {
        this.nombreOriginal = nombreOriginal;
    }

    public String getTipoMime() {
        return tipoMime;
    }

    public void setTipoMime(String tipoMime) {
        this.tipoMime = tipoMime;
    }

    public byte[] getArchivoBinario() {
        return archivoBinario;
    }

    public void setArchivoBinario(byte[] archivoBinario) {
        this.archivoBinario = archivoBinario;
    }

    public Date getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(Date fechaSubida) {
        this.fechaSubida = fechaSubida;
    }
}