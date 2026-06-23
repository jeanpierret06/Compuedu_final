package com.proyecto.AccesoUsuarios.Utils;

import java.awt.Color;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.proyecto.AccesoUsuarios.model.Inscripcion;

import jakarta.servlet.http.HttpServletResponse;

public class ComprobanteExporterPDF {

    private Inscripcion inscripcion;

    public ComprobanteExporterPDF(Inscripcion inscripcion) {
        this.inscripcion = inscripcion;
    }

    public void exportar(HttpServletResponse response) throws DocumentException, IOException {
        Document documento = new Document(PageSize.A4);
        PdfWriter.getInstance(documento, response.getOutputStream());

        documento.open();

            // 1. Definición de Colores Institucionales (Basados en tu Login)
            Color grisOscuro = new Color(52, 58, 64); // #343a40 del botón Ingresar
            Color azulTitulo = new Color(44, 62, 80);
            
            // 2. Estilos de Fuente
            Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, azulTitulo);
            Font fuenteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            Font fuenteNormal = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
            Font fuenteNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, grisOscuro);

            // --- ENCABEZADO ---
            PdfPTable tablaEncabezado = new PdfPTable(1);
            tablaEncabezado.setWidthPercentage(100);
            
            PdfPCell celdaTitulo = new PdfPCell(new Phrase("COMPROBANTE DE INSCRIPCIÓN", fuenteTitulo));
            celdaTitulo.setBorder(Rectangle.NO_BORDER);
            celdaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaTitulo.setPaddingBottom(20);
            tablaEncabezado.addCell(celdaTitulo);
            
            documento.add(tablaEncabezado);

            // --- CUERPO DEL COMPROBANTE (Usando Tablas para Diseño) ---
            PdfPTable tablaDatos = new PdfPTable(2);
            tablaDatos.setWidthPercentage(100);
            tablaDatos.setSpacingBefore(10);
            tablaDatos.setWidths(new float[] {1, 2}); // Proporción de columnas

            // Sección: Información del Estudiante
            agregarCeldaEncabezado(tablaDatos, "INFORMACIÓN DEL ESTUDIANTE", fuenteSubtitulo, grisOscuro);
            
            agregarFilaDato(tablaDatos, "Nombre completo:", inscripcion.getUsuario().getUserName(), fuenteNegrita, fuenteNormal);
            agregarFilaDato(tablaDatos, "Correo electrónico:", (inscripcion.getUsuario().getEmail() != null ? inscripcion.getUsuario().getEmail() : "N/A"), fuenteNegrita, fuenteNormal);
            agregarFilaDato(tablaDatos, "ID Registro:", String.valueOf(inscripcion.getId()), fuenteNegrita, fuenteNormal);

            // Sección: Detalles de la Convocatoria
            agregarCeldaEncabezado(tablaDatos, "DETALLES DE LA CONVOCATORIA", fuenteSubtitulo, grisOscuro);
            
            agregarFilaDato(tablaDatos, "Programa/Título:", inscripcion.getConvocatoria().getTitulo(), fuenteNegrita, fuenteNormal);
            agregarFilaDato(tablaDatos, "Descripción:", inscripcion.getConvocatoria().getDescripcion(), fuenteNegrita, fuenteNormal);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            agregarFilaDato(tablaDatos, "Fecha de Registro:", inscripcion.getFechaInscripcion().format(formatter), fuenteNegrita, fuenteNormal);

            documento.add(tablaDatos);

            // --- PIE DE PÁGINA ---
            Paragraph pie = new Paragraph("\n\nEste documento sirve como constancia oficial de su registro en el sistema Compuedu.", 
                                        FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY));
            pie.setAlignment(Element.ALIGN_CENTER);
            documento.add(pie);

            documento.close();
        }

        // Métodos auxiliares para mantener el código limpio
        private void agregarCeldaEncabezado(PdfPTable tabla, String texto, Font fuente, Color colorFondo) {
            PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
            celda.setColspan(2);
            celda.setBackgroundColor(colorFondo);
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            celda.setPadding(8);
            celda.setBorder(Rectangle.NO_BORDER);
            tabla.addCell(celda);
        }

        private void agregarFilaDato(PdfPTable tabla, String etiqueta, String valor, Font fNegrita, Font fNormal) {
            PdfPCell c1 = new PdfPCell(new Phrase(etiqueta, fNegrita));
            c1.setPadding(8);
            c1.setBorderColor(Color.LIGHT_GRAY);
            tabla.addCell(c1);

            PdfPCell c2 = new PdfPCell(new Phrase(valor, fNormal));
            c2.setPadding(8);
            c2.setBorderColor(Color.LIGHT_GRAY);
            tabla.addCell(c2);
        }    
}