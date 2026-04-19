package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.service.ConfiguracionService;
import cr.ac.una.astroline.service.SucursalService;
import java.net.URL;
import java.time.Duration;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

/**
 * Controller placeholder del módulo Kiosko. Será implementado por todos.
 *
 *
 */
public class ProyeccionController extends Controller implements Initializable {

    @FXML
    private Label lblFechaActual;
    @FXML
    private Label lblHoraActual;
    @FXML
    private Label lblNombreEmpresa;
    @FXML
    private Label lblLetraFichaLlamando;
    @FXML
    private Label lblNumeroFichaLlamando;
    @FXML
    private Label lblEstacionLlamando;
    @FXML
    private Label lblLetraFichaAnterior1;
    @FXML
    private Label lblNumeroFichaAnterior1;
    @FXML
    private Label lblEstacionFichaAnterior1;
    @FXML
    private Label lblLetraFichaAnterior2;
    @FXML
    private Label lblNumeroFichaAnterior2;
    @FXML
    private Label lblEstacionFichaAnterior2;
    @FXML
    private Label lblLetraFichaAnterior3;
    @FXML
    private Label lblNumeroFichaAnterior3;
    @FXML
    private Label lblEstacionFichaAnterior3;
    @FXML
    private Label lblLetraFichaAnterior4;
    @FXML
    private Label lblNumeroFichaAnterior4;
    @FXML
    private Label lblEstacionFichaAnterior4;
    @FXML
    private Label lblMensajeDeProyeccion;

    @Override
    public void initialize() {
        setNombreVista("Proyeccion");

        lblFechaActual.setText(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblHoraActual.setText(java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")));

        // Cargar texto de aviso de la sucursal configurada
//    String sucursalId = ConfiguracionService.getInstancia().getSucursalId();
//    Sucursal sucursal = SucursalService.getInstancia().buscarPorId(sucursalId);
//
//    String texto = (sucursal != null && !sucursal.getTextoAviso().isBlank())
//            ? sucursal.getTextoAviso()
//            : "Sin avisos disponibles.";
//
//    lblMensajeDeProyeccion.setText(texto);
//    iniciarTextoCorrente();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
}

//    private void iniciarTextoCorrente() {
//    // Esperar a que el label tenga tamaño real antes de animar
//    lblMensajeDeProyeccion.sceneProperty().addListener((obs, oldScene, newScene) -> {
//        if (newScene != null) {
//            newScene.widthProperty().addListener((o, ov, nv) -> correrTexto(nv.doubleValue()));
//            correrTexto(newScene.getWidth());
//        }
//    });
//}
//private void correrTexto(double anchoEscena) {
//    // Empieza fuera de pantalla a la derecha y termina fuera a la izquierda
//    lblMensajeDeProyeccion.setTranslateX(anchoEscena);
//
//    Timeline timeline = new Timeline(
//        new KeyFrame(Duration.ZERO,
//            new KeyValue(lblMensajeDeProyeccion.translateXProperty(), anchoEscena)
//        ),
//        new KeyFrame(Duration.seconds(18),
//            new KeyValue(lblMensajeDeProyeccion.translateXProperty(), -anchoEscena)
//        )
//    );
//    timeline.setCycleCount(Timeline.INDEFINITE);
//    timeline.play();
//}
//}
