package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.service.ConfiguracionService;
import cr.ac.una.astroline.service.EmpresaService;
import cr.ac.una.astroline.service.SucursalService;
import java.net.URL;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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
    private ImageView logoEmpresa;
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
    
    private Empresa empresa = EmpresaService.getInstancia().getEmpresa();;
    
    @Override
    public void initialize() {
        setNombreVista("Proyeccion");
        actualizarReloj();
        cargarEmpresa();
       
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
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
        DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("hh:mm:ss a");

        Timeline reloj = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> {
                    LocalDateTime ahora = LocalDateTime.now();
                    lblFechaActual.setText(ahora.format(formatoFecha));
                    lblHoraActual.setText(ahora.format(formatoHora));
                }),
                new KeyFrame(Duration.seconds(1)) // se repite cada segundo
        );

        reloj.setCycleCount(Timeline.INDEFINITE);
        reloj.play();
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
