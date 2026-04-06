package cr.ac.una.astroline.service;

import cr.ac.una.astroline.model.Cliente;
import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.model.Ficha;
import cr.ac.una.astroline.util.Respuesta;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Servicio singleton para generación de PDFs de fichas de atención.
 *
 * @author JohanDanilo
 */
public class PdfService {

    private static PdfService instancia;

    public static PdfService getInstancia() {
        if (instancia == null) instancia = new PdfService();
        return instancia;
    }

    private PdfService() {}

    // Layout
    private static final float MARGIN    = 40f;
    private static final float PAGE_W    = PDRectangle.LETTER.getWidth();
    private static final float PAGE_H    = PDRectangle.LETTER.getHeight();
    private static final float CONTENT_W = PAGE_W - 2 * MARGIN;

    // Parseo (coincide con Ficha.FORMATTER)
    private static final DateTimeFormatter PARSER    = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HORA  = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    // Paleta cosmos
    private static final Color DARK       = new Color(18,  18,  36);
    private static final Color DARK2      = new Color(26,  26,  50);
    private static final Color CYAN       = new Color(0,   212, 255);
    private static final Color CYAN_DIM   = new Color(0,   80,  110);
    private static final Color WHITE      = Color.WHITE;
    private static final Color BLACK      = new Color(30,  30,  30);
    private static final Color GRAY       = new Color(140, 140, 155);
    private static final Color GRAY_LIGHT = new Color(210, 210, 225);
    private static final Color PURPLE     = new Color(110, 40,  190);

    private static final String CARPETA = "files/fichas/";

    // ═══════════════════════════════════════════════════════════════════════════
    // API PUBLICA
    // ═══════════════════════════════════════════════════════════════════════════

    public Respuesta generarFichaPDF(Ficha ficha, Cliente cliente, Empresa empresa) {
        Respuesta r = new Respuesta();
        try {
            asegurarDir();
            File file = new File(CARPETA + buildName(ficha));

            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.LETTER);
                doc.addPage(page);

                PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                PDType1Font bold    = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font italic  = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    cs.setNonStrokingColor(WHITE);
                    cs.addRect(0, 0, PAGE_W, PAGE_H);
                    cs.fill();

                    float y = PAGE_H;
                    y = drawHeader(doc, cs, empresa, bold, regular, y);
                    y = drawTitle(cs, bold, y);
                    y = drawDateTime(cs, regular, bold, ficha, y);
                    y = drawTicketBox(cs, ficha, bold, regular, y);
                    y = drawClientSection(cs, cliente, bold, regular, y);
                        drawPreferencial(cs, ficha, bold, y);
                    drawFooter(cs, bold, italic);
                }
                doc.save(file);
            }

            r.setEstado(true);
            r.setResultado("archivo", file);
            r.setMensaje(file.getAbsolutePath());

        } catch (Exception e) {
            r.setEstado(false);
            r.setMensaje("Error PDF: " + e.getMessage());
        }
        return r;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HEADER — full bleed desde el tope de la pagina
    // ═══════════════════════════════════════════════════════════════════════════

    private float drawHeader(PDDocument doc, PDPageContentStream cs, Empresa emp,
                             PDType1Font bold, PDType1Font regular, float y) throws IOException {

        final float h     = 88f;
        final float yBase = y - h;

        // Fondo oscuro
        cs.setNonStrokingColor(DARK);
        cs.addRect(0, yBase, PAGE_W, h);
        cs.fill();

        // Franja cyan en el borde superior
        cs.setNonStrokingColor(CYAN);
        cs.addRect(0, PAGE_H - 5f, PAGE_W, 5f);
        cs.fill();

        // Dots decorativos (estrellas)
        drawHeaderDots(cs, yBase);

        // Logo desde classpath (misma logica que KioskoController)
        float xText = MARGIN;
        if (emp != null && emp.getLogoPath() != null && !emp.getLogoPath().isBlank()) {
            try {
                String ruta = "/cr/ac/una/astroline/resource/"
                        + emp.getLogoPath().replace("assets/", "");
                InputStream stream = getClass().getResourceAsStream(ruta);
                if (stream != null) {
                    PDImageXObject img = PDImageXObject.createFromByteArray(
                            doc, stream.readAllBytes(), "logo");
                    float logoSz = 52f;
                    cs.drawImage(img, MARGIN, yBase + (h - logoSz) / 2f, logoSz, logoSz);
                    xText += logoSz + 14f;
                }
            } catch (Exception ignored) {}
        }

        // Nombre empresa
        String nombre = (emp != null) ? nvl(emp.getNombre()) : "AstroLine";
        text(cs, bold, 20f, xText, yBase + 56f, nombre, CYAN);

        // Telefono | correo
        String tel    = (emp != null) ? nvl(emp.getTelefono()) : "";
        String correo = (emp != null) ? nvl(emp.getCorreo())   : "";
        if (!tel.isBlank() || !correo.isBlank()) {
            text(cs, regular, 10f, xText, yBase + 38f,
                    "Tel: " + tel + "   |   " + correo, WHITE);
        }

        // Direccion
        String dir = (emp != null) ? nvl(emp.getDireccion()) : "";
        if (!dir.isBlank()) {
            text(cs, regular, 9f, xText, yBase + 23f, dir, GRAY);
        }

        // Linea cyan inferior
        cs.setStrokingColor(CYAN);
        cs.setLineWidth(2.5f);
        cs.moveTo(0, yBase);
        cs.lineTo(PAGE_W, yBase);
        cs.stroke();

        return yBase - 28f;
    }

    private void drawHeaderDots(PDPageContentStream cs, float yBase) throws IOException {
        cs.setNonStrokingColor(CYAN_DIM);
        float[][] dots = {
            {510f, yBase + 12f, 2.5f},
            {535f, yBase + 42f, 1.5f},
            {556f, yBase + 22f, 3.0f},
            {572f, yBase + 58f, 2.0f},
            {590f, yBase + 30f, 1.5f},
            {500f, yBase + 65f, 2.0f},
            {580f, yBase + 73f, 1.2f},
            {545f, yBase + 78f, 1.8f},
        };
        for (float[] d : dots) {
            drawDot(cs, d[0], d[1], d[2]);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TITULO
    // ═══════════════════════════════════════════════════════════════════════════

    private float drawTitle(PDPageContentStream cs, PDType1Font bold, float y) throws IOException {
        center(cs, bold, 20f, y, "FICHA DE ATENCION", BLACK);
        y -= 10f;

        cs.setStrokingColor(CYAN);
        cs.setLineWidth(2.2f);
        float lw = 130f;
        cs.moveTo((PAGE_W - lw) / 2f, y);
        cs.lineTo((PAGE_W + lw) / 2f, y);
        cs.stroke();

        return y - 28f;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FECHA Y HORA — side-by-side, leidos de Ficha
    // ═══════════════════════════════════════════════════════════════════════════

    private float drawDateTime(PDPageContentStream cs, PDType1Font regular, PDType1Font bold,
                               Ficha ficha, float y) throws IOException {

        String fecha = "N/D";
        String hora  = "N/D";
        String raw   = ficha.getFechaHoraEmision();

        if (raw != null && !raw.isBlank()) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(raw, PARSER);
                fecha = ldt.format(FMT_FECHA);
                hora  = ldt.format(FMT_HORA);
            } catch (Exception ignored) {}
        }

        // Izquierda
        text(cs, regular, 9f,  MARGIN, y,       "FECHA", GRAY);
        text(cs, bold,    14f, MARGIN, y - 18f,  fecha,  BLACK);

        // Derecha alineada al margen
        float horaW = bold.getStringWidth(hora)      / 1000f * 14f;
        float labW  = regular.getStringWidth("HORA") / 1000f * 9f;
        float xR    = PAGE_W - MARGIN - Math.max(horaW, labW);
        text(cs, regular, 9f,  xR, y,       "HORA",  GRAY);
        text(cs, bold,    14f, xR, y - 18f,  hora,   BLACK);

        y -= 46f;

        cs.setStrokingColor(GRAY_LIGHT);
        cs.setLineWidth(0.5f);
        cs.moveTo(MARGIN, y + 8f);
        cs.lineTo(PAGE_W - MARGIN, y + 8f);
        cs.stroke();

        return y;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CAJA DEL NUMERO
    // ═══════════════════════════════════════════════════════════════════════════

    private float drawTicketBox(PDPageContentStream cs, Ficha ficha,
                                PDType1Font bold, PDType1Font regular, float y) throws IOException {

        final float h    = 115f;
        final float yBox = y - h;
        final float barH = 24f;

        // Fondo oscuro
        cs.setNonStrokingColor(DARK2);
        cs.addRect(MARGIN, yBox, CONTENT_W, h);
        cs.fill();

        // Banner cyan superior "TU TURNO"
        cs.setNonStrokingColor(CYAN);
        cs.addRect(MARGIN, yBox + h - barH, CONTENT_W, barH);
        cs.fill();

        center(cs, regular, 11f, yBox + h - barH + 7f, "TU TURNO", DARK);

        // Borde cyan del box completo
        cs.setStrokingColor(CYAN);
        cs.setLineWidth(2f);
        cs.addRect(MARGIN, yBox, CONTENT_W, h);
        cs.stroke();

        // Numero principal — getCodigo() = "A-001", "B-023", etc.
        center(cs, bold, 54f, yBox + 22f, ficha.getCodigo(), CYAN);

        return yBox - 28f;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SECCION CLIENTE
    // ═══════════════════════════════════════════════════════════════════════════

    private float drawClientSection(PDPageContentStream cs, Cliente cliente,
                                    PDType1Font bold, PDType1Font regular, float y) throws IOException {

        String nombre = (cliente != null) ? cliente.getNombreCompleto() : "Cliente";
        final float h = 58f;

        // Fondo sutil
        cs.setNonStrokingColor(new Color(243, 243, 252));
        cs.addRect(MARGIN, y - h, CONTENT_W, h);
        cs.fill();

        // Barra lateral izquierda
        cs.setNonStrokingColor(CYAN);
        cs.addRect(MARGIN, y - h, 4f, h);
        cs.fill();

        text(cs, regular, 9f,  MARGIN + 14f, y - 16f, "CLIENTE", GRAY);
        text(cs, bold,    16f, MARGIN + 14f, y - 36f,  nombre,   BLACK);

        return y - h - 20f;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BADGE PREFERENCIAL
    // ═══════════════════════════════════════════════════════════════════════════

    private float drawPreferencial(PDPageContentStream cs, Ficha ficha,
                                   PDType1Font bold, float y) throws IOException {

        if (!ficha.isPreferencial()) return y;

        final float h    = 36f;
        final float yBox = y - h;

        cs.setNonStrokingColor(PURPLE);
        cs.addRect(MARGIN, yBox, CONTENT_W, h);
        cs.fill();

        center(cs, bold, 13f, yBox + 11f, "ATENCION PREFERENCIAL", WHITE);

        return yBox - 18f;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FOOTER
    // ═══════════════════════════════════════════════════════════════════════════

    private void drawFooter(PDPageContentStream cs,
                            PDType1Font bold, PDType1Font italic) throws IOException {

        cs.setStrokingColor(GRAY_LIGHT);
        cs.setLineWidth(0.8f);
        cs.moveTo(MARGIN, 98f);
        cs.lineTo(PAGE_W - MARGIN, 98f);
        cs.stroke();

        cs.setNonStrokingColor(CYAN);
        drawDot(cs, PAGE_W / 2f - 18f, 90f, 2.2f);
        drawDot(cs, PAGE_W / 2f,        90f, 2.2f);
        drawDot(cs, PAGE_W / 2f + 18f,  90f, 2.2f);

        center(cs, bold,   13f, 74f, "Gracias por tu visita!",       BLACK);
        center(cs, italic, 10f, 57f, "AstroLine - Sistema de Filas", GRAY);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private void text(PDPageContentStream cs, PDType1Font font, float size,
                      float x, float y, String t, Color c) throws IOException {
        if (t == null || t.isBlank()) return;
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(c);
        cs.newLineAtOffset(x, y);
        cs.showText(t);
        cs.endText();
    }

    private void center(PDPageContentStream cs, PDType1Font font, float size,
                        float y, String t, Color c) throws IOException {
        float w = font.getStringWidth(t) / 1000f * size;
        float x = (PAGE_W - w) / 2f;
        text(cs, font, size, x, y, t, c);
    }

    /** Circulo relleno via curvas de Bezier cubicas. */
    private void drawDot(PDPageContentStream cs, float cx, float cy, float r) throws IOException {
        final float k = 0.5522848f;
        cs.moveTo(cx - r, cy);
        cs.curveTo(cx - r, cy + k * r, cx - k * r, cy + r, cx,     cy + r);
        cs.curveTo(cx + k * r, cy + r, cx + r, cy + k * r, cx + r, cy);
        cs.curveTo(cx + r, cy - k * r, cx + k * r, cy - r, cx,     cy - r);
        cs.curveTo(cx - k * r, cy - r, cx - r, cy - k * r, cx - r, cy);
        cs.closePath();
        cs.fill();
    }

    private void asegurarDir() {
        File d = new File(CARPETA);
        if (!d.exists()) d.mkdirs();
    }

    private String buildName(Ficha f) {
        return "ficha_" + System.currentTimeMillis() + ".pdf";
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }
}