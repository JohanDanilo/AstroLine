package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.App;
import cr.ac.una.astroline.model.Cliente;
import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.model.Ficha;
import cr.ac.una.astroline.model.Tramite;
import cr.ac.una.astroline.service.ClienteService;
import cr.ac.una.astroline.service.FichaService;
import cr.ac.una.astroline.service.PdfService;
import cr.ac.una.astroline.service.TramiteService;
import cr.ac.una.astroline.util.GsonUtil;
import cr.ac.una.astroline.util.Mensaje;
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


    @FXML private AnchorPane root;
    @FXML private ImageView imgLogo;
    @FXML private Label lblNombreEmpresa;
    @FXML private MFXComboBox<Tramite> cmbTramite;
    @FXML private MFXTextField txtCedula;
    @FXML private VBox panelPinAdmin;
    @FXML private MFXPasswordField passwordFldAdmin;
    @FXML private MFXButton onBtnConfirmar;
    @FXML private MFXButton btnCancelar;
    @FXML private HBox panelMensaje;
    @FXML private Label lblMensaje;

    private final FichaService fichaService = FichaService.getInstancia();
    private final Mensaje utilMensaje = new Mensaje();

    // Empresa se carga una vez en initialize() y se reutiliza en PDF
    private Empresa empresa;

    private String pinAdminCorrecto;
    private boolean preferencialPorPin = false;

    private static final ZoneId ZONA_CR = ZoneId.of("America/Costa_Rica");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // reservado para Initializable — lógica real en initialize()
    }

    // -------------------------------------------------------------------------
    // ACCIONES
    // -------------------------------------------------------------------------

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

        Respuesta respuesta = fichaService.generarFicha(
                tramiteSeleccionado.getId(),
                "sucursal-1",
                cedula.isEmpty() ? null : cedula,
                preferencial
        );

        if (!respuesta.getEstado()) {
            mostrarError(respuesta.getMensaje());
            return;
        }

        Ficha ficha = (Ficha) respuesta.getResultado("ficha");

        // ── Generar y entregar PDF ────────────────────────────────────────────
        Cliente cliente = cedula.isEmpty()
                ? null
                : ClienteService.getInstancia().buscarPorCedula(cedula);

        Respuesta respuestaPDF = PdfService.getInstancia()
                .generarFichaPDF(ficha, cliente, empresa);

        if (respuestaPDF.getEstado()) {
            File archivoPDF = (File) respuestaPDF.getResultado("archivo");
            abrirYImprimir(archivoPDF);
        } else {
            // El PDF falló pero la ficha ya fue registrada — no es error crítico
            System.err.println("[KioskoController] PDF no generado: " + respuestaPDF.getMensaje());
        }

        // ── Confirmación en pantalla ──────────────────────────────────────────
        String mensajeExito = "Tu ficha es: " + ficha.getCodigo();
        if (ficha.isPreferencial()) mensajeExito += "  (Preferencial)";
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

    // -------------------------------------------------------------------------
    // CARGA INICIAL
    // -------------------------------------------------------------------------

    private void cargarEmpresa() {
        empresa = GsonUtil.leer("empresa.json", Empresa.class);
        if (empresa == null) return;

        lblNombreEmpresa.setText(empresa.getNombre());
        pinAdminCorrecto = empresa.getPinAdmin();

        if (empresa.getLogoPath() != null && !empresa.getLogoPath().isBlank()) {
            try {
                var stream = App.class.getResourceAsStream(
                        "/cr/ac/una/astroline/resource/"
                        + empresa.getLogoPath().replace("assets/", ""));
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
            @Override public String toString(Tramite t) {
                return t == null ? "" : t.getId() + " — " + t.getNombre();
            }
            @Override public Tramite fromString(String s) { return null; }
        });
        cmbTramite.getItems().addAll(activos);
    }

    // -------------------------------------------------------------------------
    // UTILIDADES DE IMPRESIÓN / PDF
    // -------------------------------------------------------------------------

    /**
     * Abre el PDF en el visor del SO y lo envía a la impresora predeterminada.
     * Muestra la ruta guardada al usuario mediante alert informativo.
     */
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

    // -------------------------------------------------------------------------
    // UTILIDADES DE UI
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // LÓGICA DE NEGOCIO
    // -------------------------------------------------------------------------

    private boolean cedulaValida(String cedula) {
        return cedula.matches("\\d{9}");
    }

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