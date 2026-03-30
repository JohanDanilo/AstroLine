package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.App;
import cr.ac.una.astroline.model.Cliente;
import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.model.Ficha;
import cr.ac.una.astroline.model.Tramite;
import cr.ac.una.astroline.service.FichaService;
import cr.ac.una.astroline.util.GsonUtil;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import java.net.URL;
//import java.time.LocalDate;
//import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class KioskoController extends Controller implements Initializable {

    @FXML private ImageView imgLogo;
    @FXML private Label lblNombreEmpresa;
    @FXML private MFXComboBox<Tramite> cmbTramite;
    @FXML private MFXTextField txtCedula;
    @FXML private MFXPasswordField txtPinAdmin;
    @FXML private VBox panelPin;
    @FXML private Button btnGenerarFicha;
    @FXML private Button btnPin;
    @FXML private Label lblMensaje;
    @FXML private Button btnCancelarPreferencial;

    private final FichaService fichaService = new FichaService();
    private String pinAdminCorrecto;
    private boolean preferencialPorPin = false;

    private static final ZoneId ZONA_CR = ZoneId.of("America/Costa_Rica");
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public void initialize() {
        setNombreVista("Kiosko");
        cargarEmpresa();
        cargarTramites();
        limpiarFormulario();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // No se usa — la inicialización real ocurre en initialize()
    }

    // -------------------------------------------------------------------------
    // CARGA INICIAL
    // -------------------------------------------------------------------------

    private void cargarEmpresa() {
        List<Empresa> lista = GsonUtil.leerLista("empresa.json", Empresa.class);
        if (lista.isEmpty()) return;

        Empresa empresa = lista.get(0);
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
        List<Tramite> tramites = GsonUtil.leerLista("tramites.json", Tramite.class);
        List<Tramite> activos = tramites.stream()
                .filter(Tramite::isActivo)
                .toList();
        cmbTramite.setConverter(new javafx.util.StringConverter<Tramite>() {
        @Override
        public String toString(Tramite tramite) {
            return tramite == null ? "" : tramite.getId() + " — " + tramite.getNombre();
        }

        @Override
        public Tramite fromString(String string) {
            return null;
        }
    });
        cmbTramite.getItems().addAll(activos);
    }

    // -------------------------------------------------------------------------
    // ACCIONES
    // -------------------------------------------------------------------------

    @FXML
    private void onGenerarFichaAction() {
        lblMensaje.getStyleClass().removeAll("kiosko-mensaje-exito", "kiosko-mensaje-error");

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
            String mensaje = "✔ Tu ficha es: " + ficha.getCodigo();
            if (ficha.isPreferencial()) mensaje += "  ⭐ Preferencial";
            mostrarExito(mensaje);
            limpiarFormulario();
        } else {
            mostrarError(respuesta.getMensaje());
        }
    }

    @FXML
    private void onPinAction() {
        boolean visible = !panelPin.isVisible();
        panelPin.setVisible(visible);
        panelPin.setManaged(visible);
        if (!visible) {
            txtPinAdmin.clear();
            preferencialPorPin = false;
        }
    }

    @FXML
    private void onConfirmarPinAction() {
        String pinIngresado = txtPinAdmin.getText().trim();

        if (pinAdminCorrecto == null || pinAdminCorrecto.isBlank()) {
            mostrarError("No hay PIN de administrador configurado.");
            return;
        }

        if (pinIngresado.equals(pinAdminCorrecto)) {
            preferencialPorPin = true;
            panelPin.setVisible(false);
            panelPin.setManaged(false);
            txtPinAdmin.clear();
            mostrarExito("✔ Atención preferencial activada.");
        } else {
            mostrarError("PIN incorrecto.");
            txtPinAdmin.clear();
        }
    }
    
    @FXML
    private void onCancelarPreferencialAction() {
        preferencialPorPin = false;
        btnCancelarPreferencial.setVisible(false);
        btnCancelarPreferencial.setManaged(false);
        mostrarExito("Atención preferencial cancelada.");
    }

    // -------------------------------------------------------------------------
    // LÓGICA DE NEGOCIO LOCAL
    // -------------------------------------------------------------------------

    /**
     * Detecta automáticamente si el cliente es mayor de 65 años
     * buscando su fecha de nacimiento en clientes.json por cédula.
     */
    private boolean detectarPreferencial(String cedula) {
    if (cedula == null || cedula.isBlank()) return false;

    List<Cliente> clientes = GsonUtil.leerLista("clientes.json", Cliente.class);

    return clientes.stream()
            .filter(c -> cedula.equals(c.getCedula()))
            .findFirst()
            .map(Cliente::esMayorDe65)
            .orElse(false);
    }

    // -------------------------------------------------------------------------
    // UI HELPERS
    // -------------------------------------------------------------------------

    private void mostrarExito(String mensaje) {
        lblMensaje.getStyleClass().remove("kiosko-mensaje-error");
        if (!lblMensaje.getStyleClass().contains("kiosko-mensaje-exito")) {
            lblMensaje.getStyleClass().add("kiosko-mensaje-exito");
        }
        lblMensaje.setText(mensaje);
        
        // Mostrar botón cancelar solo si el preferencial está activo
        btnCancelarPreferencial.setVisible(preferencialPorPin);
        btnCancelarPreferencial.setManaged(preferencialPorPin);
    }

    private void mostrarError(String mensaje) {
        lblMensaje.getStyleClass().remove("kiosko-mensaje-exito");
        if (!lblMensaje.getStyleClass().contains("kiosko-mensaje-error")) {
            lblMensaje.getStyleClass().add("kiosko-mensaje-error");
        }
        lblMensaje.setText(mensaje);
    }

    private void limpiarFormulario() {
        cmbTramite.clearSelection();
        txtCedula.clear();
        preferencialPorPin = false;
        panelPin.setVisible(false);
        panelPin.setManaged(false);
        lblMensaje.setText("");
        lblMensaje.getStyleClass().removeAll("kiosko-mensaje-exito", "kiosko-mensaje-error");
        btnCancelarPreferencial.setVisible(false);
        btnCancelarPreferencial.setManaged(false);
    }

    private boolean cedulaValida(String cedula) {
        return cedula.matches("\\d{9}") || cedula.matches("\\d{12}");
    }
}