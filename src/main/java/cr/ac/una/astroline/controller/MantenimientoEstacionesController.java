package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.ConfiguracionLocal;
import cr.ac.una.astroline.model.Estacion;
import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.service.ConfiguracionService;
import cr.ac.una.astroline.service.SucursalService;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.Respuesta;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;

/**
 * Configuracion local del equipo para kiosko o estacion de funcionario.
 */
public class MantenimientoEstacionesController extends Controller implements Initializable {

    @FXML
    private AnchorPane root;
    @FXML
    private MFXComboBox<Sucursal> cmbSucursal;
    @FXML
    private MFXComboBox<Estacion> cmbEstacion;
    @FXML
    private MFXToggleButton tglModoKiosko;
    @FXML
    private Label lblModo;
    @FXML
    private MFXButton btnGuardar;
    @FXML
    private ImageView logoEmpresa;
    @FXML
    private Label nombreEmpresa;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarConverters();
    }

    @Override
    public void initialize() {
        setNombreVista("Configuracion local");
        cargarSucursales();
        cargarConfiguracionActual();
        actualizarModo();
    }

    @FXML
    private void onCmbSucursal(ActionEvent event) {
        cargarEstacionesDeSucursal(cmbSucursal.getValue());
    }

    @FXML
    private void onTglModo(ActionEvent event) {
        actualizarModo();
    }

    @FXML
    private void onBtnGuardar(ActionEvent event) {
        Sucursal sucursal = cmbSucursal.getValue();
        if (sucursal == null) {
            mostrarAviso("Selecciona una sucursal.");
            return;
        }

        String estacionId = null;
        if (!tglModoKiosko.isSelected()) {
            Estacion estacion = cmbEstacion.getValue();
            if (estacion == null) {
                mostrarAviso("Selecciona una estacion o activa el modo kiosko.");
                return;
            }
            estacionId = estacion.getId();
        }

        Respuesta respuesta = ConfiguracionService.getInstancia()
                .guardarConfiguracion(sucursal.getId(), estacionId);

        mostrarAviso(respuesta.getMensaje());
        if (respuesta.getEstado()) {
            getStage().close();
        }
    }

    private void configurarConverters() {
        cmbSucursal.setConverter(new StringConverter<>() {
            @Override
            public String toString(Sucursal sucursal) {
                return sucursal == null ? "" : sucursal.getNombre();
            }

            @Override
            public Sucursal fromString(String string) {
                return null;
            }
        });

        cmbEstacion.setConverter(new StringConverter<>() {
            @Override
            public String toString(Estacion estacion) {
                return estacion == null ? "" : estacion.getNombre();
            }

            @Override
            public Estacion fromString(String string) {
                return null;
            }
        });
    }

    private void cargarSucursales() {
        cmbSucursal.getItems().setAll(SucursalService.getInstancia().getListaDeSucursales());
    }

    private void cargarEstacionesDeSucursal(Sucursal sucursal) {
        cmbEstacion.getItems().clear();
        cmbEstacion.setValue(null);
        if (sucursal != null) {
            cmbEstacion.getItems().addAll(sucursal.getEstaciones());
        }
        actualizarModo();
    }

    private void cargarConfiguracionActual() {
        ConfiguracionLocal configuracion = ConfiguracionService.getInstancia().getConfiguracion();
        if (configuracion == null || configuracion.getSucursalId() == null) {
            return;
        }

        Sucursal sucursal = SucursalService.getInstancia().buscarSucursal(configuracion.getSucursalId());
        if (sucursal == null) {
            return;
        }

        cmbSucursal.setValue(sucursal);
        cargarEstacionesDeSucursal(sucursal);

        if (configuracion.getEstacionId() == null || configuracion.getEstacionId().isBlank()) {
            tglModoKiosko.setSelected(true);
        } else {
            Estacion estacion = sucursal.buscarEstacion(configuracion.getEstacionId());
            if (estacion != null) {
                cmbEstacion.setValue(estacion);
            }
            tglModoKiosko.setSelected(false);
        }
    }

    private void actualizarModo() {
        boolean modoKiosko = tglModoKiosko.isSelected();
        cmbEstacion.setDisable(modoKiosko);
        if (modoKiosko) {
            cmbEstacion.setValue(null);
            lblModo.setText("Modo actual: Kiosko");
        } else {
            lblModo.setText("Modo actual: Estacion de funcionario");
        }
    }

    private void mostrarAviso(String mensaje) {
        Controller controller = FlowController.getInstance().getController("AvisoView");
        if (controller instanceof AvisoController avisoController) {
            avisoController.cambiarInformacionDeAviso(mensaje);
        }
        FlowController.getInstance().goViewInWindowModal(
                "AvisoView",
                FlowController.getInstance().getMainStage(),
                false
        );
    }
}
