package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Cliente;
import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.model.Ficha;
import cr.ac.una.astroline.model.Tramite;
import cr.ac.una.astroline.service.ClienteService;
import cr.ac.una.astroline.service.ConfiguracionService;
import cr.ac.una.astroline.service.EmpresaService;
import cr.ac.una.astroline.service.FichaService;
import cr.ac.una.astroline.service.PdfService;
import cr.ac.una.astroline.service.TramiteService;
import cr.ac.una.astroline.util.Mensaje;
import cr.ac.una.astroline.util.PathManager;
import cr.ac.una.astroline.util.Respuesta;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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

    private final FichaService fichaService = FichaService.getInstancia();
    private final Mensaje utilMensaje = new Mensaje();
    private final Empresa empresa = EmpresaService.getInstancia().getEmpresa();
    
    
    
    private String pinAdminCorrecto;
    private boolean preferencialPorPin = false;

    private static final ZoneId ZONA_CR = ZoneId.of("America/Costa_Rica");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

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
        panelMensaje.setVisible(false);
        panelMensaje.setManaged(false);
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
        String sucursalId = ConfiguracionService.getInstancia().getSucursalId();

        Respuesta respuesta = fichaService.generarFicha(
            tramiteSeleccionado.getId(),
            sucursalId,
            cedula.isEmpty() ? null : cedula,
            preferencial
        );

        if (!respuesta.getEstado()) {
            mostrarError(respuesta.getMensaje());
            return;
        }

        Ficha ficha = (Ficha) respuesta.getResultado("ficha");

        /*Generar PDF*/
        Cliente cliente = cedula.isEmpty() ? null : ClienteService.getInstancia().buscarPorCedula(cedula);

        Respuesta respuestaPDF = PdfService.getInstancia().generarFichaPDF(ficha, cliente);

        if (respuestaPDF.getEstado()) {
            File archivoPDF = (File) respuestaPDF.getResultado("archivo");
            abrirYImprimir(archivoPDF);
        } else {

            System.err.println("[KioskoController] PDF no generado: " + respuestaPDF.getMensaje());
        }

        String mensajeExito = "Tu ficha es: " + ficha.getCodigo();
        if (ficha.isPreferencial()) {
            mensajeExito += "  (Preferencial)";
        }
        mostrarExito(mensajeExito);

        limpiarFormulario();
    }

    @FXML
    private void onBtnConfirmar(ActionEvent event) {
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

    private void cargarEmpresa() {

        if (empresa == null) {
            return;
        }

        lblNombreEmpresa.setText(empresa.getNombre());

        this.pinAdminCorrecto = empresa.getPinAdmin();

        if (empresa.getLogoPath() != null && !empresa.getLogoPath().isBlank()) {
            try {
                String nombreSolo = new java.io.File(empresa.getLogoPath()).getName();
                java.io.File archivoLogo = PathManager.getDataPath().resolve("logoEmpresa").resolve(nombreSolo).toFile();

                if (archivoLogo.exists()) {
                    imgLogo.setImage(new Image(archivoLogo.toURI().toString()));
                } else {
                    System.err.println("[KioskoController] No encontrado en: " + archivoLogo.getAbsolutePath());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void cargarTramites() {
        List<Tramite> activos = TramiteService.getInstancia().getTramitesActivos();
        cmbTramite.setConverter(new javafx.util.StringConverter<Tramite>() {
            @Override
            public String toString(Tramite t) {
                return t == null ? "" : t.getId() + " — " + t.getNombre();
            }

            @Override
            public Tramite fromString(String s) {
                return null;
            }
        });
        cmbTramite.getItems().addAll(activos);
    }

    private void abrirYImprimir(File pdf) {
        if (!Desktop.isDesktopSupported()) {
            System.err.println("[KioskoController] java.awt.Desktop no disponible.");
            return;
        }
        Desktop desktop = Desktop.getDesktop();
        try {
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(pdf);
            }
            if (desktop.isSupported(Desktop.Action.PRINT)) {
                desktop.print(pdf);
            }
            utilMensaje.show(AlertType.INFORMATION,
                    "PDF Generado",
                    "Ficha guardada en:\n" + pdf.getAbsolutePath());
        } catch (IOException ex) {
            System.err.println("[KioskoController] No se pudo abrir/imprimir el PDF: " + ex.getMessage());
        }
    }

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
        utilMensaje.show(AlertType.INFORMATION, "Trámite Exitoso", mensaje);
        btnCancelar.setVisible(preferencialPorPin);
        btnCancelar.setManaged(preferencialPorPin);
    }

    private void mostrarError(String mensaje) {
        utilMensaje.show(AlertType.ERROR, "Atención", mensaje);
    }

    private void mostrarExitoPin(String mensaje) {
        lblMensaje.setText(mensaje);
        panelMensaje.setVisible(true);
        panelMensaje.setManaged(true);
        btnCancelar.setVisible(preferencialPorPin);
        btnCancelar.setManaged(preferencialPorPin);
    }

    private void mostrarErrorPin(String mensaje) {
        lblMensaje.setText(mensaje);
        panelMensaje.setVisible(true);
        panelMensaje.setManaged(true);
    }

    @FXML
    private void onBtnNumero(ActionEvent event) {
        MFXButton btn = (MFXButton) event.getSource();
        if (panelPinAdmin.isVisible()) {
            String actual = passwordFldAdmin.getText();
            passwordFldAdmin.setText(actual + btn.getText());
        } else {
            String actual = txtCedula.getText();
            if (actual.length() < 12) {
                txtCedula.setText(actual + btn.getText());
            }
        }
    }

    @FXML
    private void onBtnBorrar(ActionEvent event) {
        if (panelPinAdmin.isVisible()) {
            String actual = passwordFldAdmin.getText();
            if (!actual.isEmpty()) {
                passwordFldAdmin.setText(actual.substring(0, actual.length() - 1));
            }
        } else {
            String actual = txtCedula.getText();
            if (!actual.isEmpty()) {
                txtCedula.setText(actual.substring(0, actual.length() - 1));
            }
        }
    }

    private boolean cedulaValida(String cedula) {
        return cedula.matches("\\d{9}");
    }

    private boolean detectarPreferencial(String cedula) {
        if (cedula == null || cedula.isBlank()) {
            return false;
        }
        Cliente cliente = ClienteService.getInstancia().buscarPorCedula(cedula);
        return cliente != null && cliente.esMayorDe65();
    }

}
