package pe.edu.continental.planilla.infraestructura.web;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import pe.edu.continental.planilla.dominio.modelo.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public final class GeneradorBoletaPdf {

    private static final Color AZUL_PRIMARIO = new Color(24, 54, 97);
    private static final Color FONDO_SUAVE = new Color(241, 245, 249);
    private static final Color BORDE_GRIS = new Color(203, 213, 225);

    private static final Font F_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, AZUL_PRIMARIO);
    private static final Font F_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
    private static final Font F_TH = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font F_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
    private static final Font F_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
    private static final Font F_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, AZUL_PRIMARIO);

    private GeneradorBoletaPdf() {}

    public static byte[] generar(BoletaPago boleta, Empresa empresa) {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, salida);
            doc.open();

            Trabajador t = boleta.trabajador();
            PeriodoPlanilla p = boleta.periodo();
            String nombreMes = Month.of(p.mes())
                    .getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-PE"))
                    .toUpperCase();

            // 1. Encabezado de empresa y título
            PdfPTable tablaCabecera = new PdfPTable(new float[]{62f, 38f});
            tablaCabecera.setWidthPercentage(100);

            PdfPCell celdaEmpresa = new PdfPCell();
            celdaEmpresa.setBorder(Rectangle.NO_BORDER);
            celdaEmpresa.addElement(new Paragraph(empresa.razonSocial(), F_TITULO));
            celdaEmpresa.addElement(new Paragraph("RUC: " + empresa.ruc(), F_BOLD));
            if (empresa.direccion() != null && !empresa.direccion().isBlank()) {
                celdaEmpresa.addElement(new Paragraph(empresa.direccion(), F_NORMAL));
            }
            tablaCabecera.addCell(celdaEmpresa);

            PdfPCell celdaBoleta = new PdfPCell();
            celdaBoleta.setBackgroundColor(FONDO_SUAVE);
            celdaBoleta.setBorderColor(BORDE_GRIS);
            celdaBoleta.setPadding(8f);
            Paragraph pTitulo = new Paragraph("BOLETA DE PAGO", F_SUBTITULO);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            Paragraph pNum = new Paragraph("N° " + String.format("%06d", boleta.idBoleta()), F_TITULO);
            pNum.setAlignment(Element.ALIGN_CENTER);
            Paragraph pPer = new Paragraph("Período: " + nombreMes + " " + p.anio(), F_BOLD);
            pPer.setAlignment(Element.ALIGN_CENTER);
            celdaBoleta.addElement(pTitulo);
            celdaBoleta.addElement(pNum);
            celdaBoleta.addElement(pPer);
            tablaCabecera.addCell(celdaBoleta);

            doc.add(tablaCabecera);
            doc.add(new Paragraph(" "));

            // 2. Datos del trabajador
            PdfPTable tablaTrabajador = new PdfPTable(new float[]{22f, 33f, 22f, 23f});
            tablaTrabajador.setWidthPercentage(100);
            agregarDato(tablaTrabajador, "Trabajador:", t.nombreCompleto());
            agregarDato(tablaTrabajador, "DNI:", t.dni());
            agregarDato(tablaTrabajador, "Cargo:", t.cargo() != null ? t.cargo() : "-");
            agregarDato(tablaTrabajador, "Fecha ingreso:", t.fechaIngreso().toString());
            String regimen = t.sistemaPension().nombre()
                    + (t.tipoComision() != null ? " (" + t.tipoComision().name() + ")" : "");
            agregarDato(tablaTrabajador, "Régimen pensión:", regimen);
            agregarDato(tablaTrabajador, "Fecha emisión:", boleta.fechaEmision().toString());
            doc.add(tablaTrabajador);
            doc.add(new Paragraph(" "));

            // 3. Detalle de ingresos, descuentos y aportes
            PdfPTable tablaDetalle = new PdfPTable(new float[]{46f, 20f, 14f, 20f});
            tablaDetalle.setWidthPercentage(100);
            agregarEncabezado(tablaDetalle, "Concepto", Element.ALIGN_LEFT);
            agregarEncabezado(tablaDetalle, "Tipo", Element.ALIGN_CENTER);
            agregarEncabezado(tablaDetalle, "Tasa (%)", Element.ALIGN_RIGHT);
            agregarEncabezado(tablaDetalle, "Monto (S/)", Element.ALIGN_RIGHT);

            for (DetalleBoleta d : boleta.detalles()) {
                agregarCelda(tablaDetalle, d.concepto(), F_NORMAL, Element.ALIGN_LEFT, Color.WHITE);
                agregarCelda(tablaDetalle, d.tipo().name(), F_NORMAL, Element.ALIGN_CENTER, Color.WHITE);
                String tasa = d.porcentaje() != null ? d.porcentaje() + " %" : "-";
                agregarCelda(tablaDetalle, tasa, F_NORMAL, Element.ALIGN_RIGHT, Color.WHITE);
                agregarCelda(tablaDetalle, moneda(d.monto()), F_NORMAL, Element.ALIGN_RIGHT, Color.WHITE);
            }
            doc.add(tablaDetalle);
            doc.add(new Paragraph(" "));

            // 4. Resumen de totales
            PdfPTable tablaTotales = new PdfPTable(new float[]{70f, 30f});
            tablaTotales.setWidthPercentage(55);
            tablaTotales.setHorizontalAlignment(Element.ALIGN_RIGHT);
            agregarFilaTotal(tablaTotales, "Remuneración Bruta:", moneda(boleta.remuneracionBruta()), false);
            agregarFilaTotal(tablaTotales, "Total Descuentos:", "- " + moneda(boleta.totalDescuentos()), false);
            agregarFilaTotal(tablaTotales, "NETO A PAGAR:", moneda(boleta.netoPagar()), true);
            agregarFilaTotal(tablaTotales, "Aporte Empleador (EsSalud):", moneda(boleta.aporteEssalud()), false);
            doc.add(tablaTotales);

            doc.add(new Paragraph("\n\n"));
            Paragraph firma = new Paragraph("_______________________________\nEmpleador / Área de Planillas", F_NORMAL);
            firma.setAlignment(Element.ALIGN_CENTER);
            doc.add(firma);

            doc.close();
            return salida.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF de la boleta", e);
        }
    }

    private static void agregarDato(PdfPTable tabla, String etiqueta, String valor) {
        agregarCelda(tabla, etiqueta, F_BOLD, Element.ALIGN_LEFT, FONDO_SUAVE);
        agregarCelda(tabla, valor, F_NORMAL, Element.ALIGN_LEFT, Color.WHITE);
    }

    private static void agregarEncabezado(PdfPTable tabla, String texto, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, F_TH));
        celda.setBackgroundColor(AZUL_PRIMARIO);
        celda.setBorderColor(AZUL_PRIMARIO);
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(6f);
        tabla.addCell(celda);
    }

    private static void agregarCelda(PdfPTable tabla, String texto, Font fuente, int alineacion, Color fondo) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBackgroundColor(fondo);
        celda.setBorderColor(BORDE_GRIS);
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(5f);
        tabla.addCell(celda);
    }

    private static void agregarFilaTotal(PdfPTable tabla, String etiqueta, String valor, boolean destacado) {
        Font f = destacado ? F_TOTAL : F_BOLD;
        Color fondo = destacado ? FONDO_SUAVE : Color.WHITE;
        agregarCelda(tabla, etiqueta, f, Element.ALIGN_LEFT, fondo);
        agregarCelda(tabla, valor, f, Element.ALIGN_RIGHT, fondo);
    }

    private static String moneda(BigDecimal monto) {
        return "S/ " + String.format(Locale.US, "%,.2f", monto);
    }
}
