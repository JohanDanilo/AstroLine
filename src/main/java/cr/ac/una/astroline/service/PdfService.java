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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Servicio singleton para generación de PDFs de fichas de atención.
 * Formato ticket compacto
 *
 * @author JohanDanilo
 */
public class PdfService {

    private static PdfService instancia;

    public static PdfService getInstancia() {
        if (instancia == null) instancia = new PdfService();
        return instancia;
    }
    
    Empresa empresa = EmpresaService.getInstancia().getEmpresa();

    private PdfService() {}

    // ── Tamaño de página: 3.5" × 5.3" ──────────────────────────────────────
    private static final PDRectangle PAGE_SIZE = new PDRectangle(250f, 410f);

    // Layout
    private static final float MARGIN    = 20f;
    private static final float PAGE_W    = PAGE_SIZE.getWidth();    // 252
    private static final float PAGE_H    = PAGE_SIZE.getHeight();   // 382
    private static final float CONTENT_W = PAGE_W - 2 * MARGIN;    // 212

    // Para centrar texto visualmente: baseline = centerY - size * CAP_RATIO
    private static final float CAP_RATIO = 0.36f;

    // Parseo
    private static final DateTimeFormatter PARSER    = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HORA  = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter FMT_FILE  = DateTimeFormatter.ofPattern("ddMMyyyy");

    // Paleta cosmos
    private static final Color DARK       = new Color(18,  18,  36);
    private static final Color DARK2      = new Color(26,  26,  50);
    private static final Color CYAN       = new Color(0,   212, 255);
    private static final Color CYAN_DIM   = new Color(0,   70,  100);
    private static final Color WHITE      = Color.WHITE;
    private static final Color BLACK      = new Color(30,  30,  30);
    private static final Color GRAY       = new Color(140, 140, 155);
    private static final Color GRAY_LIGHT = new Color(210, 210, 225);
    private static final Color PURPLE     = new Color(110, 40,  190);

    private static final String CARPETA = "files/fichas/";

    // ═══════════════════════════════════════════════════════════════════════════
    // API PUBLICA
    // ═══════════════════════════════════════════════════════════════════════════

    public Respuesta generarFichaPDF(Ficha ficha, Cliente cliente) {
        Respuesta r = new Respuesta();
        try {
            asegurarDir();
            File file = new File(CARPETA + buildName(ficha));

            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage(PAGE_SIZE);
                doc.addPage(page);

                PDType0Font regular;
                PDType0Font bold;
                PDType0Font italic;

                try (InputStream regularStream = getClass().getResourceAsStream("/cr/ac/una/astroline/resource/fonts/GoogleSans-Regular.ttf");
                     InputStream boldStream    = getClass().getResourceAsStream("/cr/ac/una/astroline/resource/fonts/GoogleSans-Bold.ttf");
                     InputStream italicStream  = getClass().getResourceAsStream("/cr/ac/una/astroline/resource/fonts/GoogleSans-Italic.ttf")) {

                    regular = PDType0Font.load(doc, regularStream);
                    bold    = PDType0Font.load(doc, boldStream);
                    italic  = PDType0Font.load(doc, italicStream);
                }

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
    // HEADER
    // ═══════════════════════════════════════════════════════════════════════════

    private float drawHeader(PDDocument doc, PDPageContentStream cs, Empresa emp,
                             PDType0Font bold, PDType0Font regular, float y) throws IOException {

        final float HEADER_H = 70f;
        final float LOGO_SZ  = 42f;
        final float yBase    = y - HEADER_H;
        final float logoX    = MARGIN;
        // Logo centrado verticalmente en el header
        final float logoY    = yBase + (HEADER_H - LOGO_SZ) / 2f;

        // ── Fondo ────────────────────────────────────────────────────────────
        cs.setNonStrokingColor(DARK);
        cs.addRect(0, yBase, PAGE_W, HEADER_H);
        cs.fill();

        // Franja cyan top
        cs.setNonStrokingColor(CYAN);
        cs.addRect(0, PAGE_H - 4f, PAGE_W, 4f);
        cs.fill();

        // ── Constelación en zona derecha ─────────────────────────────────────
        drawHeaderDots(cs, yBase, HEADER_H);

        // ── Logo ─────────────────────────────────────────────────────────────
        float xText  = MARGIN;
        
        boolean hasLogo = false;

        try {
            InputStream logoStream = null;

            // 1. Intentar logo externo subido por el usuario
            File logoExterno = new File("data/logoEmpresa/logo_empresa.png");
            if (logoExterno.exists() && logoExterno.isFile()) {
                logoStream = new java.io.FileInputStream(logoExterno);
            }

            // 2. Fallback al logo empaquetado en el JAR
            if (logoStream == null) {
                logoStream = getClass().getResourceAsStream(
                        "/cr/ac/una/astroline/resource/LogoEmpresa.png");
            }

            if (logoStream != null) {
                PDImageXObject img = PDImageXObject.createFromByteArray(
                        doc, logoStream.readAllBytes(), "logo");
                logoStream.close();
                cs.drawImage(img, logoX, logoY, LOGO_SZ, LOGO_SZ);
                xText   = logoX + LOGO_SZ + 12f;
                hasLogo = true;
            }

        } catch (Exception e) {
            // Logo no cargó, el texto del header arranca desde MARGIN normalmente
            System.err.println("PdfService: no se pudo cargar el logo: " + e.getMessage());
        }

        // ── Bloque de texto: centrado verticalmente en el header ──────────────
        // Recopilamos líneas disponibles
        String nombre = nvl(emp != null ? emp.getNombre() : "AstroLine");
        String tel    = nvl(emp != null ? emp.getTelefono() : "");
        String correo = nvl(emp != null ? emp.getCorreo()   : "");
        String dir    = nvl(emp != null ? emp.getDireccion(): "");

        boolean hasTelLine = !tel.isBlank() || !correo.isBlank();
        boolean hasDirLine = !dir.isBlank();

        final float SZ_NAME = 14f;
        final float SZ_TEL  = 8f;
        final float SZ_DIR  = 7.5f;
        final float LINE_GAP = 6f;

        // Altura total del bloque: suma de cap-heights + gaps entre líneas
        float blockH = SZ_NAME * 0.72f;
        if (hasTelLine) blockH += LINE_GAP + SZ_TEL  * 0.72f;
        if (hasDirLine) blockH += LINE_GAP + SZ_DIR  * 0.72f;

        // Centro vertical del header
        float headerCY = yBase + HEADER_H / 2f;

        // Baseline de la primera línea: sube blockH/2 desde el centro,
        // luego baja el cap-height del nombre para que la línea base quede bien
        float nameBaseline = headerCY + blockH / 2f - SZ_NAME * 0.72f;

        text(cs, bold, SZ_NAME, xText, nameBaseline, nombre, CYAN);

        if (hasTelLine) {
            String telLine = (!tel.isBlank() && !correo.isBlank())
                    ? "Tel: " + tel + "   " + correo
                    : (!tel.isBlank() ? "Tel: " + tel : correo);
            float telBase = nameBaseline - SZ_NAME * 0.72f - LINE_GAP;
            text(cs, regular, SZ_TEL, xText, telBase, telLine, WHITE);

            if (hasDirLine) {
                float dirBase = telBase - SZ_TEL * 0.72f - LINE_GAP;
                text(cs, regular, SZ_DIR, xText, dirBase, dir, GRAY);
            }
        }

        // ── Línea cyan inferior ───────────────────────────────────────────────
        cs.setStrokingColor(CYAN);
        cs.setLineWidth(2f);
        cs.moveTo(0, yBase);
        cs.lineTo(PAGE_W, yBase);
        cs.stroke();

        return yBase - 16f;
    }

    /**
     * 8 estrellas en el tercio derecho del header con 3 líneas de constelación.
     * Las coordenadas X están dentro de [170, 248] para no solapar el texto.
     */
    private void drawHeaderDots(PDPageContentStream cs, float yBase, float h) throws IOException {
        // {x, y_offset_desde_yBase, radio}
        float[][] stars = {
            {175f, h * 0.76f, 1.0f},
            {192f, h * 0.52f, 1.8f},
            {207f, h * 0.82f, 1.2f},
            {216f, h * 0.28f, 2.2f},
            {226f, h * 0.62f, 1.5f},
            {234f, h * 0.16f, 1.0f},
            {241f, h * 0.46f, 1.8f},
            {247f, h * 0.78f, 1.2f},
        };

        // Líneas de constelación (tenues)
        cs.setStrokingColor(CYAN_DIM);
        cs.setLineWidth(0.4f);
        int[][] lines = {{1, 3}, {3, 6}, {4, 6}, {6, 7}};
        for (int[] l : lines) {
            cs.moveTo(stars[l[0]][0], yBase + stars[l[0]][1]);
            cs.lineTo(stars[l[1]][0], yBase + stars[l[1]][1]);
            cs.stroke();
        }

        // Puntos encima
        cs.setNonStrokingColor(CYAN_DIM);
        for (float[] s : stars) {
            drawDot(cs, s[0], yBase + s[1], s[2]);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TITULO
    // ═══════════════════════════════════════════════════════════════════════════

    private float drawTitle(PDPageContentStream cs, PDType0Font bold, float y) throws IOException {
        center(cs, bold, 14f, y, "FICHA DE ATENCION", BLACK);
        y -= 7f;

        cs.setStrokingColor(CYAN);
        cs.setLineWidth(2f);
        float lw = 96f;
        cs.moveTo((PAGE_W - lw) / 2f, y);
        cs.lineTo((PAGE_W + lw) / 2f, y);
        cs.stroke();

        return y - 18f;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FECHA Y HORA
    // ═══════════════════════════════════════════════════════════════════════════

    private float drawDateTime(PDPageContentStream cs, PDType0Font regular, PDType0Font bold,
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

        text(cs, regular, 8f,  MARGIN, y,       "FECHA", GRAY);
        text(cs, bold,    12f, MARGIN, y - 14f,  fecha,  BLACK);

        float horaW = bold.getStringWidth(hora)      / 1000f * 12f;
        float labW  = regular.getStringWidth("HORA") / 1000f * 8f;
        float xR    = PAGE_W - MARGIN - Math.max(horaW, labW);
        text(cs, regular, 8f,  xR, y,       "HORA",  GRAY);
        text(cs, bold,    12f, xR, y - 14f,  hora,   BLACK);

        y -= 30f;

        cs.setStrokingColor(GRAY_LIGHT);
        cs.setLineWidth(0.5f);
        cs.moveTo(MARGIN, y + 4f);
        cs.lineTo(PAGE_W - MARGIN, y + 4f);
        cs.stroke();

        return y;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CAJA DEL NUMERO
    // ═══════════════════════════════════════════════════════════════════════════

    private float drawTicketBox(PDPageContentStream cs, Ficha ficha,
                                PDType0Font bold, PDType0Font regular, float y) throws IOException {

        final float BOX_H = 88f;
        final float BAR_H = 20f;
        final float yBox  = y - BOX_H;

        // Fondo oscuro
        cs.setNonStrokingColor(DARK2);
        cs.addRect(MARGIN, yBox, CONTENT_W, BOX_H);
        cs.fill();

        // Banner "TU TURNO"
        cs.setNonStrokingColor(CYAN);
        cs.addRect(MARGIN, yBox + BOX_H - BAR_H, CONTENT_W, BAR_H);
        cs.fill();

        // "TU TURNO" centrado verticalmente en el banner
        float bannerCY   = yBox + BOX_H - BAR_H + BAR_H / 2f;
        float turnoBase  = bannerCY - 10f * CAP_RATIO;
        center(cs, regular, 10f, turnoBase, "TU TURNO", DARK);

        // Borde cyan
        cs.setStrokingColor(CYAN);
        cs.setLineWidth(2f);
        cs.addRect(MARGIN, yBox, CONTENT_W, BOX_H);
        cs.stroke();

        // Número centrado verticalmente en la zona oscura
        float darkH   = BOX_H - BAR_H;
        float darkCY  = yBox + darkH / 2f;
        float numBase = darkCY - 44f * CAP_RATIO;
        center(cs, bold, 44f, numBase, ficha.getCodigo(), CYAN);

        return yBox - 14f;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SECCION CLIENTE
    // ═══════════════════════════════════════════════════════════════════════════

    private float drawClientSection(PDPageContentStream cs, Cliente cliente,
                                    PDType0Font bold, PDType0Font regular, float y) throws IOException {

        String nombre = (cliente != null) ? cliente.getNombreCompleto() : "Cliente";
        final float H = 42f;

        cs.setNonStrokingColor(new Color(243, 243, 252));
        cs.addRect(MARGIN, y - H, CONTENT_W, H);
        cs.fill();

        cs.setNonStrokingColor(CYAN);
        cs.addRect(MARGIN, y - H, 4f, H);
        cs.fill();

        // Centrar las dos líneas verticalmente en la caja
        float boxCY     = (y - H) + H / 2f;
        float labelBase = boxCY + 7f;
        float nameBase  = boxCY - 8f;

        text(cs, regular, 8f,  MARGIN + 12f, labelBase, "CLIENTE", GRAY);
        text(cs, bold,    13f, MARGIN + 12f, nameBase,   nombre,   BLACK);

        return y - H - 12f;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BADGE PREFERENCIAL
    // ═══════════════════════════════════════════════════════════════════════════

    private float drawPreferencial(PDPageContentStream cs, Ficha ficha,
                                   PDType0Font bold, float y) throws IOException {

        if (!ficha.isPreferencial()) return y;

        final float H    = 30f;
        final float yBox = y - H;

        cs.setNonStrokingColor(PURPLE);
        cs.addRect(MARGIN, yBox, CONTENT_W, H);
        cs.fill();

        float textBase = yBox + H / 2f - 11f * CAP_RATIO;
        center(cs, bold, 11f, textBase, "ATENCION PREFERENCIAL", WHITE);

        return yBox - 10f;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FOOTER
    // ═══════════════════════════════════════════════════════════════════════════

    private void drawFooter(PDPageContentStream cs,
                            PDType0Font bold, PDType0Font italic) throws IOException {

        cs.setStrokingColor(GRAY_LIGHT);
        cs.setLineWidth(0.8f);
        cs.moveTo(MARGIN, 55f);
        cs.lineTo(PAGE_W - MARGIN, 55f);
        cs.stroke();

        cs.setNonStrokingColor(CYAN);
        drawDot(cs, PAGE_W / 2f - 12f, 46f, 2f);
        drawDot(cs, PAGE_W / 2f,        46f, 2f);
        drawDot(cs, PAGE_W / 2f + 12f,  46f, 2f);

        center(cs, bold,   11f, 34f, "Gracias por tu visita!",       BLACK);
        center(cs, italic,  9f, 20f, "AstroLine - Sistema de Filas", GRAY);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private void text(PDPageContentStream cs, PDType0Font font, float size,
                      float x, float y, String t, Color c) throws IOException {
        if (t == null || t.isBlank()) return;
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(c);
        cs.newLineAtOffset(x, y);
        cs.showText(t);
        cs.endText();
    }

    private void center(PDPageContentStream cs, PDType0Font font, float size,
                        float y, String t, Color c) throws IOException {
        float w = font.getStringWidth(t) / 1000f * size;
        float x = (PAGE_W - w) / 2f;
        text(cs, font, size, x, y, t, c);
    }

    /** Círculo relleno via curvas de Bézier cúbicas. */
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
        String codigo = f.getCodigo() != null ? f.getCodigo().replace("-", "") : "XXX";
        String fecha  = LocalDate.now().format(FMT_FILE);
        
        ZoneId zonaHoraria = ZoneId.systemDefault();
        LocalTime horaActual = LocalTime.now(zonaHoraria);
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("hhmma", Locale.US);
        String hora = horaActual.format(formato);
        
        return codigo + "_" + fecha + "_" + hora + ".pdf";
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }
}