package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.model.Estacion;
import cr.ac.una.astroline.model.Ficha;
import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.service.ConfiguracionService;
import cr.ac.una.astroline.service.EmpresaService;
import cr.ac.una.astroline.service.FichaService;
import cr.ac.una.astroline.service.SucursalService;
import cr.ac.una.astroline.util.Respuesta;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ProyeccionController extends Controller implements Initializable {

    // ── FXML ────────────────────────────────────────────────────────────────
    @FXML private Label lblFechaActual;
    @FXML private Label lblHoraActual;
    @FXML private Label lblNombreEmpresa;
    @FXML private ImageView logoEmpresa;
    @FXML private Label lblLetraFichaLlamando;
    @FXML private Label lblNumeroFichaLlamando;
    @FXML private Label lblEstacionLlamando;
    @FXML private Label lblLetraFichaAnterior1;
    @FXML private Label lblNumeroFichaAnterior1;
    @FXML private Label lblEstacionFichaAnterior1;
    @FXML private Label lblLetraFichaAnterior2;
    @FXML private Label lblNumeroFichaAnterior2;
    @FXML private Label lblEstacionFichaAnterior2;
    @FXML private Label lblLetraFichaAnterior3;
    @FXML private Label lblNumeroFichaAnterior3;
    @FXML private Label lblEstacionFichaAnterior3;
    @FXML private Label lblLetraFichaAnterior4;
    @FXML private Label lblNumeroFichaAnterior4;
    @FXML private Label lblEstacionFichaAnterior4;
    @FXML private Label lblMensajeDeProyeccion;
    @FXML private Pane paneMarquee;  // ← nuevo, requiere el cambio en el FXML

    // ── Servicios ────────────────────────────────────────────────────────────
    private final Empresa empresa = EmpresaService.getInstancia().getEmpresa();
    private final FichaService fichaService = FichaService.getInstancia(); // ← singleton

    // ── Marquee ──────────────────────────────────────────────────────────────
    /**
     * Dirección del texto en movimiento.
     * DERECHA_A_IZQUIERDA: texto entra por la derecha y sale por la izquierda (estándar).
     * IZQUIERDA_A_DERECHA: ticker inverso.
     */
    public enum DireccionMarquee {
        DERECHA_A_IZQUIERDA,
        IZQUIERDA_A_DERECHA
    }

    private DireccionMarquee direccionMarquee = DireccionMarquee.DERECHA_A_IZQUIERDA;
    private double velocidadPixelesPorSegundo = 80.0; // ajustable
    private Timeline timelineMarquee;

    // ── Formato fechaHoraLlamado ─────────────────────────────────────────────
    private static final DateTimeFormatter FORMATO_LLAMADO =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    // ── Inicialización ───────────────────────────────────────────────────────


    // ── Marquee ──────────────────────────────────────────────────────────────
    /**
     * Dirección del texto en movimiento.
     * DERECHA_A_IZQUIERDA: texto entra por la derecha y sale por la izquierda (estándar).
     * IZQUIERDA_A_DERECHA: ticker inverso.
     */
    public enum DireccionMarquee {
        DERECHA_A_IZQUIERDA,
        IZQUIERDA_A_DERECHA
    }

    private DireccionMarquee direccionMarquee = DireccionMarquee.DERECHA_A_IZQUIERDA;
    private double velocidadPixelesPorSegundo = 80.0; // ajustable
    private Timeline timelineMarquee;

    // ── Inicialización ───────────────────────────────────────────────────────


    @Override
    public void initialize() {
        setNombreVista("Proyeccion");
        actualizarReloj();
        cargarEmpresa();
        cargarMensajeProyeccion();
        iniciarPollerFichas();
        iniciarMarquee();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    // ── Empresa ──────────────────────────────────────────────────────────────

    private void cargarEmpresa() {
        if (empresa == null) return;

        lblNombreEmpresa.setText(empresa.getNombre());

        if (empresa.getLogoPath() != null && !empresa.getLogoPath().isBlank()) {
            try {
                String nombreSolo = new java.io.File(empresa.getLogoPath()).getName();
                java.io.File archivoLogo = new java.io.File("data/logoEmpresa/" + nombreSolo);
                if (archivoLogo.exists()) {
                    logoEmpresa.setImage(new Image(archivoLogo.toURI().toString()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ── Reloj ────────────────────────────────────────────────────────────────

    private void actualizarReloj() {
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatoHora  = DateTimeFormatter.ofPattern("hh:mm:ss a");

        Timeline reloj = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> {
                    LocalDateTime ahora = LocalDateTime.now();
                    lblFechaActual.setText(ahora.format(formatoFecha));
                    lblHoraActual.setText(ahora.format(formatoHora));
                }),
                new KeyFrame(Duration.seconds(1))
        );
        reloj.setCycleCount(Timeline.INDEFINITE);
        reloj.play();
    }

    // ── Fichas ───────────────────────────────────────────────────────────────

    private void iniciarPollerFichas() {
        Timeline pollerFichas = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> actualizarFichasEnPantalla())
        );
        pollerFichas.setCycleCount(Timeline.INDEFINITE);
        pollerFichas.play();
    }

    private void actualizarFichasEnPantalla() {
        Respuesta respuesta = fichaService.obtenerFichasActivas();
        if (!respuesta.getEstado()) return;

        List<Ficha> activas = (List<Ficha>) respuesta.getResultado("lista");

        List<Ficha> llamadas = activas.stream()
                .filter(f -> f.getEstado() == Ficha.Estado.LLAMADA
                          && f.getFechaHoraLlamado() != null)
                .sorted(Comparator.comparing(
                        f -> LocalDateTime.parse(f.getFechaHoraLlamado(), FORMATO_LLAMADO),
                        Comparator.reverseOrder()))
                .collect(Collectors.toList());

        mostrarFichaActual(llamadas.size() > 0 ? llamadas.get(0) : null);
        mostrarFichaAnterior(lblLetraFichaAnterior1, lblNumeroFichaAnterior1, lblEstacionFichaAnterior1, llamadas.size() > 1 ? llamadas.get(1) : null);
        mostrarFichaAnterior(lblLetraFichaAnterior2, lblNumeroFichaAnterior2, lblEstacionFichaAnterior2, llamadas.size() > 2 ? llamadas.get(2) : null);
        mostrarFichaAnterior(lblLetraFichaAnterior3, lblNumeroFichaAnterior3, lblEstacionFichaAnterior3, llamadas.size() > 3 ? llamadas.get(3) : null);
        mostrarFichaAnterior(lblLetraFichaAnterior4, lblNumeroFichaAnterior4, lblEstacionFichaAnterior4, llamadas.size() > 4 ? llamadas.get(4) : null);
    }

    private void mostrarFichaActual(Ficha ficha) {
        if (ficha == null) {
            lblLetraFichaLlamando.setText("-");
            lblNumeroFichaLlamando.setText("-");
            lblEstacionLlamando.setText("-");
            return;
        }
        lblLetraFichaLlamando.setText(fichaService.getCodigoLetra(ficha));
        lblNumeroFichaLlamando.setText(ficha.getNumeroFormateado());
        lblEstacionLlamando.setText(resolverNombreEstacion(ficha.getEstacionId()));
    }

    private void mostrarFichaAnterior(Label lblLetra, Label lblNumero, Label lblEstacion, Ficha ficha) {
        if (ficha == null) {
            lblLetra.setText("-");
            lblNumero.setText("-");
            lblEstacion.setText("-");
            return;
        }
        lblLetra.setText(fichaService.getCodigoLetra(ficha));
        lblNumero.setText(ficha.getNumeroFormateado());
        lblEstacion.setText(resolverNombreEstacion(ficha.getEstacionId()));
    }

    private String resolverNombreEstacion(String estacionId) {
        if (estacionId == null) return "-";
        Estacion estacion = SucursalService.getInstancia().buscarEstacion(estacionId);
        return estacion != null ? estacion.getNombre() : estacionId;
    }

    // ── Marquee ──────────────────────────────────────────────────────────────

    private void cargarMensajeProyeccion() {
        String mensaje = "";

        // leer la configuración local de esta máquina
        var config = ConfiguracionService.getInstancia().getConfiguracion();

        if (config != null && config.getSucursalId() != null 
                           && !config.getSucursalId().isBlank()) {

            // buscar la sucursal configurada
            Sucursal sucursal = SucursalService.getInstancia().buscarSucursal(config.getSucursalId());

            // leer su textoAviso
            if (sucursal != null && sucursal.getTextoAviso() != null 
                                 && !sucursal.getTextoAviso().isBlank()) {
                mensaje = sucursal.getTextoAviso();
            }
        }

        // Paso 4: fallback si la máquina no está configurada o el aviso está vacío
        if (mensaje.isBlank()) {
            mensaje = "Bienvenido — Gracias por preferirnos";
        }

        lblMensajeDeProyeccion.setText(mensaje);
    }

    /**
     * Arranca el marquee una vez que la ventana está visible,
     * momento en el que el Pane ya tiene un ancho real.
     */
    private void iniciarMarquee() {
        // Centramos verticalmente el label dentro del Pane
        lblMensajeDeProyeccion.layoutYProperty().bind(
                paneMarquee.heightProperty()
                           .subtract(lblMensajeDeProyeccion.heightProperty())
                           .divide(2)
        );

        // Clip para que el texto no se "derrame" fuera del panel
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(paneMarquee.widthProperty());
        clip.heightProperty().bind(paneMarquee.heightProperty());
        paneMarquee.setClip(clip);

        // Esperamos a que la ventana esté mostrándose para tener anchos reales
        paneMarquee.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
            if (newScene == null) return;
            newScene.windowProperty().addListener((obsWin, oldWin, newWin) -> {
                if (newWin == null) return;
                newWin.showingProperty().addListener((obsShowing, waShowing, isShowing) -> {
                    if (isShowing) arrancarMarquee();
                });
            });
        });
    }

    /**
     * Calcula posición de inicio/fin según dirección y lanza (o reinicia) la animación.
     * Se puede llamar desde fuera para actualizar velocidad o dirección en caliente.
     */
    private void arrancarMarquee() {
        if (timelineMarquee != null) timelineMarquee.stop();

        // Forzamos layout para obtener el ancho real del texto
        lblMensajeDeProyeccion.applyCss();
        lblMensajeDeProyeccion.layout();

        double anchoLabel      = lblMensajeDeProyeccion.getLayoutBounds().getWidth();
        double anchoContenedor = paneMarquee.getWidth();

        double posInicio, posFin;

        if (direccionMarquee == DireccionMarquee.DERECHA_A_IZQUIERDA) {
            posInicio = anchoContenedor;          // entra por la derecha
            posFin    = -anchoLabel;              // sale por la izquierda
        } else {
            posInicio = -anchoLabel;              // entra por la izquierda
            posFin    = anchoContenedor;          // sale por la derecha
        }

        double distancia       = Math.abs(posFin - posInicio);
        double duracionSegundos = distancia / velocidadPixelesPorSegundo;

        timelineMarquee = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(lblMensajeDeProyeccion.translateXProperty(), posInicio)),
                new KeyFrame(Duration.seconds(duracionSegundos),
                        new KeyValue(lblMensajeDeProyeccion.translateXProperty(), posFin))
        );
        timelineMarquee.setCycleCount(Timeline.INDEFINITE);
        timelineMarquee.play();
    }

    // ── API pública para configurar el marquee ───────────────────────────────

    /**
     * Cambia la velocidad del marquee en tiempo real.
     * @param pixelesPorSegundo  Ej: 60 = lento, 120 = rápido. Default: 80.
     */
    public void setVelocidadMarquee(double pixelesPorSegundo) {
        this.velocidadPixelesPorSegundo = pixelesPorSegundo;
        if (paneMarquee.getScene() != null) arrancarMarquee();
    }

    /**
     * Cambia la dirección del marquee en tiempo real.
     * @param direccion  DERECHA_A_IZQUIERDA | IZQUIERDA_A_DERECHA
     */
    public void setDireccionMarquee(DireccionMarquee direccion) {
        this.direccionMarquee = direccion;
        if (paneMarquee.getScene() != null) arrancarMarquee();
    }
}