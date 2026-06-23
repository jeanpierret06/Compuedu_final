package com.proyecto.AccesoUsuarios.Utils;

import java.awt.Color;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.proyecto.AccesoUsuarios.model.Usuario;

import jakarta.servlet.http.HttpServletResponse;

public class UsuarioExporterPDF {

    private final List<Usuario> listaUsuarios;

    public UsuarioExporterPDF(List<Usuario> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
    }

    private void escribirCabeceraDeLaTabla(PdfPTable tabla) {
        PdfPCell celda = new PdfPCell();
        celda.setBackgroundColor(new Color(31, 41, 55)); // Gris azulado oscuro
        celda.setPadding(7);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Font fuente = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fuente.setColor(Color.WHITE);
        fuente.setSize(9);

        // Columnas actualizadas para incluir Nombres y Apellidos por separado
        String[] columnas = {"ID", "USUARIO", "NOMBRES", "APELLIDOS", "EMAIL", "DOCUMENTO", "ROL", "ESTADO"};

        for (String columna : columnas) {
            celda.setPhrase(new Phrase(columna, fuente));
            tabla.addCell(celda);
        }
    }

    private void escribirDatosDeLaTabla(PdfPTable tabla) {
        Font fuenteDatos = FontFactory.getFont(FontFactory.HELVETICA);
        fuenteDatos.setSize(8);

        for (Usuario usuario : listaUsuarios) {
            // ID
            PdfPCell celdaId = new PdfPCell(new Phrase(String.valueOf(usuario.getId()), fuenteDatos));
            celdaId.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(celdaId);

            // Datos principales
            tabla.addCell(crearCeldaDato(usuario.getUserName(), fuenteDatos));
            tabla.addCell(crearCeldaDato(usuario.getNombre(), fuenteDatos)); //
            tabla.addCell(crearCeldaDato(usuario.getApellido(), fuenteDatos)); //
            tabla.addCell(crearCeldaDato(usuario.getEmail(), fuenteDatos));
            tabla.addCell(crearCeldaDato(usuario.getDocumento(), fuenteDatos));

            // Rol centrado
            PdfPCell celdaRol = crearCeldaDato(usuario.getRol(), fuenteDatos);
            celdaRol.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(celdaRol);
            
            // Estado con estilo de etiqueta (Badge)
            String textoEstado = usuario.isHabilitado() ? "ACTIVO" : "INACTIVO";
            Color colorEstado = usuario.isHabilitado() ? new Color(22, 163, 74) : new Color(220, 38, 38);
            
            Font fuenteEstado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, colorEstado);
            PdfPCell celdaEstado = new PdfPCell(new Phrase(textoEstado, fuenteEstado));
            celdaEstado.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaEstado.setPadding(5);
            tabla.addCell(celdaEstado);
        }
    }

    private PdfPCell crearCeldaDato(String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto != null ? texto : "N/D", fuente));
        celda.setPadding(5);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return celda;
    }

    public void exportar(HttpServletResponse response) throws DocumentException, IOException {
        Document documento = new Document(PageSize.A4.rotate(), 30, 30, 40, 40);
        PdfWriter.getInstance(documento, response.getOutputStream());

        documento.open();

        // --- ENCABEZADO "COMPUEDU" ---
        Font fuenteProyecto = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(78, 115, 223));
        Paragraph proyecto = new Paragraph("SISTEMA ACADÉMICO - COMPUEDU", fuenteProyecto); //
        proyecto.setAlignment(Paragraph.ALIGN_LEFT);
        documento.add(proyecto);

        // --- TÍTULO DEL REPORTE ---
        Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
        Paragraph p = new Paragraph("Listado Detallado de Usuarios", fuenteTitulo);
        p.setAlignment(Paragraph.ALIGN_CENTER);
        p.setSpacingBefore(-10);
        p.setSpacingAfter(10);
        documento.add(p);

        // --- LÍNEA DE SEPARACIÓN ---
        Paragraph linea = new Paragraph("________________________________________________________________________________________________________________");
        linea.setSpacingAfter(15);
        documento.add(linea);

        // --- FECHA Y CONTEO ---
        Font fuenteInfo = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
        String fechaActual = new SimpleDateFormat("dd/MM/yyyy HH:mm a").format(new Date());
        Paragraph info = new Paragraph("Generado el: " + fechaActual + " | Total de registros encontrados: " + listaUsuarios.size(), fuenteInfo);
        info.setSpacingAfter(20);
        documento.add(info);

        // --- TABLA (8 columnas ahora) ---
        PdfPTable tabla = new PdfPTable(8);
        tabla.setWidthPercentage(100f);
        // Ajuste de anchos para que quepan los nombres y apellidos
        tabla.setWidths(new float[] {0.6f, 1.4f, 1.8f, 1.8f, 2.5f, 1.4f, 1.2f, 1.2f});

        escribirCabeceraDeLaTabla(tabla);
        escribirDatosDeLaTabla(tabla);

        documento.add(tabla);
        documento.close();
    }
}