package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.model.Estacion;
import cr.ac.una.astroline.model.Ficha;
import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.service.AudioService;
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
    @FXML private Pane paneMensaje;

    private final Empresa empresa = EmpresaService.getInstancia().getEmpresa();
    private final FichaService fichaService = FichaService.getInstancia();
    private final AudioService audioService = AudioService.getInstancia();

    @Override
    public void initialize() {
    }

    public enum DireccionMarquee {
        DERECHA_A_IZQUIERDA,
        IZQUIERDA_A_DERECHA
    }

    private DireccionMarquee direccionMensaje = DireccionMarquee.DERECHA_A_IZQUIERDA;
    private double velocidadPixelesPorSegundo = 80.0;
    private Timeline tiempoMensaje;

    private String ultimaFichaAnunciadaId = null;
    private String ultimoTimestampAnunciado = null;

    private static final DateTimeFormatter FORMATO_LLAMADO = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setNombreVista("Proyeccion");
        actualizarReloj();
        cargarEmpresa();
        cargarMensajeProyeccion();
        iniciarPollerFichas();
        iniciarProyeccionMensaje();
    }

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

    private void iniciarPollerFichas() {
        Timeline pollerFichas = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> actualizarFichasEnPantalla()));
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

        Ficha fichaActual = llamadas.size() > 0 ? llamadas.get(0) : null;

        anunciarSiEsNueva(fichaActual);

        mostrarFichaActual(fichaActual);
        mostrarFichaAnterior(lblLetraFichaAnterior1, lblNumeroFichaAnterior1, lblEstacionFichaAnterior1, llamadas.size() > 1 ? llamadas.get(1) : null);
        mostrarFichaAnterior(lblLetraFichaAnterior2, lblNumeroFichaAnterior2, lblEstacionFichaAnterior2, llamadas.size() > 2 ? llamadas.get(2) : null);
        mostrarFichaAnterior(lblLetraFichaAnterior3, lblNumeroFichaAnterior3, lblEstacionFichaAnterior3, llamadas.size() > 3 ? llamadas.get(3) : null);
        mostrarFichaAnterior(lblLetraFichaAnterior4, lblNumeroFichaAnterior4, lblEstacionFichaAnterior4, llamadas.size() > 4 ? llamadas.get(4) : null);
    }

    private void anunciarSiEsNueva(Ficha ficha) {
        if (ficha == null) {
            ultimaFichaAnunciadaId = null;
            ultimoTimestampAnunciado = null;
            return;
        }

        String idActual = ficha.getId();
        String timestampActual = ficha.getFechaHoraLlamado();

        // Si es la misma ficha Y el mismo timestamp → ya fue anunciada, no repetir
        if (idActual.equals(ultimaFichaAnunciadaId)
                && timestampActual != null
                && timestampActual.equals(ultimoTimestampAnunciado)) {
            return;
        }

        ultimaFichaAnunciadaId = idActual;
        ultimoTimestampAnunciado = timestampActual;

        String letra = fichaService.getCodigoLetra(ficha);
        String numero = ficha.getNumeroFormateado();
        String estacion = resolverNombreEstacion(ficha.getEstacionId());

        audioService.anunciarFicha(letra, numero, estacion);
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

        Estacion estacion = SucursalService.getInstancia().buscarEstacion(estacionId);
        if (estacion == null || estacionId.isBlank()) {
            String numero = estacionId.substring(estacionId.lastIndexOf("-") + 1);
            return "Estacion " + numero;
        }
        return estacion.getNombre();
    }

    private void cargarMensajeProyeccion() {
        String mensaje = "";

        var config = ConfiguracionService.getInstancia().getConfiguracion();

        if (config != null && config.getSucursalId() != null&& !config.getSucursalId().isBlank()) {
            Sucursal sucursal = SucursalService.getInstancia().buscarSucursal(config.getSucursalId());

            if (sucursal != null && sucursal.getTextoAviso() != null && !sucursal.getTextoAviso().isBlank()) {
                mensaje = sucursal.getTextoAviso();
            }
        }

        if (mensaje.isBlank()) {
            mensaje = "Bienvenido — Gracias por preferirnos";
        }

        lblMensajeDeProyeccion.setText(mensaje);
    }

    private void iniciarProyeccionMensaje() {
        lblMensajeDeProyeccion.layoutYProperty().bind( paneMensaje.heightProperty().subtract(lblMensajeDeProyeccion.heightProperty()).divide(2) );

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(paneMensaje.widthProperty());
        clip.heightProperty().bind(paneMensaje.heightProperty());
        paneMensaje.setClip(clip);

        paneMensaje.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
            if (newScene == null) return;
            newScene.windowProperty().addListener((obsWin, oldWin, newWin) -> {
                if (newWin == null) return;
                newWin.showingProperty().addListener((obsShowing, waShowing, isShowing) -> {
                    if (isShowing) arrancarProyeccionMensaje();
                });
            });
        });
    }

    private void arrancarProyeccionMensaje() {
        if (tiempoMensaje != null) tiempoMensaje.stop();

        lblMensajeDeProyeccion.applyCss();
        lblMensajeDeProyeccion.layout();

        double anchoLabel = lblMensajeDeProyeccion.getLayoutBounds().getWidth();
        double anchoContenedor = paneMensaje.getWidth();

        double posInicio, posFin;

        if (direccionMensaje == DireccionMarquee.DERECHA_A_IZQUIERDA) {
            posInicio = anchoContenedor;
            posFin = -anchoLabel;
        } else {
            posInicio = -anchoLabel;
            posFin = anchoContenedor;
        }

        double distancia        = Math.abs(posFin - posInicio);
        double duracionSegundos = distancia / velocidadPixelesPorSegundo;

        tiempoMensaje = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(lblMensajeDeProyeccion.translateXProperty(), posInicio)),
                new KeyFrame(Duration.seconds(duracionSegundos),new KeyValue(lblMensajeDeProyeccion.translateXProperty(), posFin)));
        tiempoMensaje.setCycleCount(Timeline.INDEFINITE);
        tiempoMensaje.play();
    }

    public void setVelocidadMensaje(double pixelesPorSegundo) {
        this.velocidadPixelesPorSegundo = pixelesPorSegundo;
        if (paneMensaje.getScene() != null) arrancarProyeccionMensaje();
    }

    public void setDireccionMensaje(DireccionMarquee direccion) {
        this.direccionMensaje = direccion;
        if (paneMensaje.getScene() != null) arrancarProyeccionMensaje();
    }

    public void setAudioHabilitado(boolean habilitado) {
        audioService.setHabilitado(habilitado);
    }
}