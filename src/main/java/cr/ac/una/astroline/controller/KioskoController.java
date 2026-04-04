package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.App;
import cr.ac.una.astroline.model.Cliente;
import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.model.Ficha;
import cr.ac.una.astroline.model.Tramite;
import cr.ac.una.astroline.service.ClienteService;
import cr.ac.una.astroline.service.FichaService;
import cr.ac.una.astroline.service.TramiteService;
import cr.ac.una.astroline.util.GsonUtil;
import cr.ac.una.astroline.util.Mensaje;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


/**
 * FXML Controller class
 *
 * @author JohanDanilo
 */
public class KioskoController extends Controller implements Initializable {


    @FXML
    private AnchorPane root;
    @FXML
    private ImageView imgLogo;
    @FXML
    private Label lblNombreEmpresa;
    @FXML
    private MFXComboBox<Tramite> cmbTramite;
    @FXML
    private MFXTextField txtCedula;
    @FXML
    private VBox panelPinAdmin;
    @FXML
    private MFXPasswordField passwordFldAdmin;
    @FXML
    private MFXButton onBtnConfirmar;
    
    @FXML
    private MFXButton btnCancelar;
    
    @FXML
    private HBox panelMensaje;
    @FXML
    private Label lblMensaje;
    
    private final FichaService fichaService = new FichaService();
    private final Mensaje utilMensaje = new Mensaje();
    private String pinAdminCorrecto;
    private boolean preferencialPorPin = false;

    private static final ZoneId ZONA_CR = ZoneId.of("America/Costa_Rica");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    // -------------------------------------------------------------------------//
    // ACCIONES                                                                 //
    // -------------------------------------------------------------------------//
    
    @FXML
    private void onBtnPreferencial(ActionEvent event) {
        boolean isVisible = panelPinAdmin.isVisible();

        panelPinAdmin.setVisible(!isVisible);
        panelPinAdmin.setManaged(!isVisible);

        if (isVisible) {
            passwordFldAdmin.clear();
        }
    }
    
    @FXML
    private void onBtnCancelar(ActionEvent event) {
        preferencialPorPin = false;
        btnCancelar.setVisible(false);
        btnCancelar.setManaged(false);
        mostrarExito("Atención preferencial cancelada.");
    }

    @FXML
    private void onCmbTramite(ActionEvent event) {
        cmbTramite.show();
    }

    @FXML
    private void onBtnObtenerFicha(ActionEvent event) {

        Tramite tramiteSeleccionado = cmbTramite.getValue();
        if (tramiteSeleccionado == null) {
            mostrarError("Seleccioná un trámite antes de continuar.");
            return;
        }

        String cedula = txtCedula.getText().trim();
        if (!cedula.isEmpty() && !cedulaValida(cedula)) {
            mostrarError("La cédula debe tener 9 dígitos o 12 dígitos (DIMEX).");
            return;
        }

        boolean preferencial = preferencialPorPin || detectarPreferencial(cedula);

        // Usamos "sucursal-1" como id fijo por ahora — se puede parametrizar luego
        var respuesta = fichaService.generarFicha(
                tramiteSeleccionado.getId(),
                "sucursal-1",
                cedula.isEmpty() ? null : cedula,
                preferencial
        );

        if (respuesta.getEstado()) {
            Ficha ficha = (Ficha) respuesta.getResultado("ficha");
            String mensaje = "Tu ficha es: " + ficha.getCodigo();
            if (ficha.isPreferencial()) mensaje += "  Preferencial";
            mostrarExito(mensaje);
            limpiarFormulario();
        } else {
            mostrarError(respuesta.getMensaje());
        }
    }
    
    @FXML
    private void onBtnConfirmar(ActionEvent event){
        String pinIngresado = passwordFldAdmin.getText().trim();

        if (pinAdminCorrecto == null || pinAdminCorrecto.isBlank()) {
            mostrarErrorPin("No hay PIN de administrador configurado.");
            return;
        }

        if (pinIngresado.equals(pinAdminCorrecto)) {
            preferencialPorPin = true;
            panelPinAdmin.setVisible(false);
            panelPinAdmin.setManaged(false);
            passwordFldAdmin.clear();
            mostrarExitoPin("Atención preferencial activada.");
        } else {
            mostrarErrorPin("PIN incorrecto.");
            passwordFldAdmin.clear();
        }
    }

    @Override
    public void initialize() {
        setNombreVista("Kiosko");
        cargarEmpresa();
        cargarTramites();
        limpiarFormulario();
    }
    
    // -------------------------------------------------------------------------//
    // CARGA INICIAL                                                            //
    // -------------------------------------------------------------------------//

    private void cargarEmpresa() {
        Empresa empresa = GsonUtil.leer("empresa.json", Empresa.class);
        if (empresa == null) return;
        lblNombreEmpresa.setText(empresa.getNombre());
        pinAdminCorrecto = empresa.getPinAdmin();

        if (empresa.getLogoPath() != null && !empresa.getLogoPath().isBlank()) {
            try {
                var stream = App.class.getResourceAsStream(
                        "/cr/ac/una/astroline/resource/" + empresa.getLogoPath().replace("assets/", ""));
                if (stream != null) {
                    imgLogo.setImage(new Image(stream));
                }
            } catch (Exception e) {
                System.err.println("[KioskoController] Logo no encontrado: " + e.getMessage());
            }
        }
    }
    
    private void cargarTramites() {
        List<Tramite> activos = TramiteService.getInstancia().getTramitesActivos();
        cmbTramite.setConverter(new javafx.util.StringConverter<Tramite>() {
            @Override
            public String toString(Tramite tramite) {
                return tramite == null ? "" : tramite.getId() + " — " + tramite.getNombre();
            }
            @Override
            public Tramite fromString(String string) { return null; }
        });
        cmbTramite.getItems().addAll(activos);
    }
    
    // -------------------------------------------------------------------------//
    // Utilidades                                                               //
    // -------------------------------------------------------------------------//
    
    private void limpiarFormulario() {
        cmbTramite.clearSelection();
        txtCedula.clear();
        preferencialPorPin = false;
        panelPinAdmin.setVisible(false);
        panelPinAdmin.setManaged(false);
        lblMensaje.setText("");
        panelMensaje.setVisible(false);
        panelMensaje.setManaged(false);  
        btnCancelar.setVisible(false);
        btnCancelar.setManaged(false);
    }
    
    private void mostrarExito(String mensaje) {
        // Llamamos a la ventana emergente de éxito
        utilMensaje.show(AlertType.INFORMATION, "Trámite Exitoso", mensaje);

        // Mantenemos tu lógica de la interfaz
        btnCancelar.setVisible(preferencialPorPin);
        btnCancelar.setManaged(preferencialPorPin);
    }

    private void mostrarError(String mensaje) {
        // Llamamos a la ventana emergente de error
        utilMensaje.show(AlertType.ERROR, "Atención", mensaje);
    }
    
    private void mostrarExitoPin(String mensaje) {
        /** Usar para limpiar clase de error y poner la de éxito cuando esté en el css
        lblMensaje.getStyleClass().remove("kiosko-mensaje-error");
        if (!lblMensaje.getStyleClass().contains("kiosko-mensaje-exito")) {
            lblMensaje.getStyleClass().add("kiosko-mensaje-exito");
        }*/
        lblMensaje.setText(mensaje);

        panelMensaje.setVisible(true);
        panelMensaje.setManaged(true);

        btnCancelar.setVisible(preferencialPorPin);
        btnCancelar.setManaged(preferencialPorPin);
    }

    private void mostrarErrorPin(String mensaje) {
        /** Usar para limpiar clase de exito y poner la de error cuando esté en el css
        lblMensaje.getStyleClass().remove("kiosko-mensaje-exito");
        if (!lblMensaje.getStyleClass().contains("kiosko-mensaje-error")) {
            lblMensaje.getStyleClass().add("kiosko-mensaje-error");
        }
        */
        lblMensaje.setText(mensaje);
        panelMensaje.setVisible(true);
        panelMensaje.setManaged(true);
    }

    

    private boolean cedulaValida(String cedula) {
        return cedula.matches("\\d{9}");
    }
    
    // -------------------------------------------------------------------------
    // LÓGICA DE NEGOCIO
    // -------------------------------------------------------------------------

    /**
     * Detecta automáticamente si el cliente es mayor de 65 años
     * buscando su fecha de nacimiento en clientes.json por cédula.
     */
    private boolean detectarPreferencial(String cedula) {
        if (cedula == null || cedula.isBlank()) return false;
        Cliente cliente = ClienteService.getInstancia().buscarPorCedula(cedula);
        return cliente != null && cliente.esMayorDe65();
    }

}