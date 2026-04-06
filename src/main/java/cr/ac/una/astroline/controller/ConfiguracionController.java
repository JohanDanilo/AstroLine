package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.ConfiguracionLocal;
import cr.ac.una.astroline.model.Estacion;
import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.service.ConfiguracionService;
import cr.ac.una.astroline.service.SucursalService;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.Respuesta;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;

public class ConfiguracionController extends Controller implements Initializable {

    @FXML private AnchorPane root;
    @FXML private MFXComboBox<Sucursal> cmbSucursal;
    @FXML private MFXComboBox<Estacion> cmbEstacion;
    @FXML private MFXToggleButton tglModulo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @Override
    public void initialize() {
        configurarConverters();
        cargarSucursales();
        cargarConfiguracionActual();
    }

    @FXML
    private void onCmbSucursal(ActionEvent event) {
        Sucursal seleccionada = cmbSucursal.getValue();
        cmbEstacion.getItems().clear();
        cmbEstacion.clearSelection();
        if (seleccionada == null) return;
        cmbEstacion.getItems().addAll(seleccionada.getEstaciones());
        cmbEstacion.show();
    }

    @FXML
    private void onCmbEstacion(ActionEvent event)  {
        Estacion seleccionada = cmbEstacion.getValue();
        if (seleccionada == null) return;
        // Si la estación es preferencial, reflejarlo en el toggle o en un label
        // Por ahora queda preparado para cuando haya más UI en esta vista
    }

    @FXML
    private void onTglModulo(ActionEvent event) {
        boolean esKiosko = tglModulo.isSelected();
        cmbEstacion.setDisable(esKiosko);
        if (esKiosko) cmbEstacion.clearSelection();
    }

    @FXML
    private void onBtnGuardar(ActionEvent event) {
        Sucursal sucursal = cmbSucursal.getValue();
        if (sucursal == null) {
            mostrarError("Seleccioná una sucursal.");
            return;
        }

        String estacionId = null;
        if (!tglModulo.isSelected()) {
            Estacion estacion = cmbEstacion.getValue();
            if (estacion == null) {
                mostrarError("Seleccioná una estación o activá el modo Kiosko.");
                return;
            }
            estacionId = estacion.getId();
        }

        Respuesta r = ConfiguracionService.getInstancia()
                .guardarConfiguracion(sucursal.getId(), estacionId);

        if (r.getEstado()) {
            mostrarExito("Configuración guardada correctamente.");
        } else {
            mostrarError(r.getMensaje());
        }
    }
    
    private void configurarConverters() {
        cmbSucursal.setConverter(new StringConverter<Sucursal>() {
            @Override public String toString(Sucursal s) {
                return s == null ? "" : s.getNombre();
            }
            @Override public Sucursal fromString(String s) { return null; }
        });
        cmbEstacion.setConverter(new StringConverter<Estacion>() {
            @Override public String toString(Estacion e) {
                return e == null ? "" : e.getNombre();
            }
            @Override public Estacion fromString(String s) { return null; }
        });
    }

    private void cargarSucursales() {
        cmbSucursal.getItems().addAll(SucursalService.getInstancia().getListaDeSucursales());
    }

    private void cargarConfiguracionActual() {
        ConfiguracionLocal config = ConfiguracionService.getInstancia().getConfiguracion();

        // Primera vez: archivo vacío, no preseleccionar nada
        if (config == null || config.getSucursalId() == null) return;

        String sucursalId = config.getSucursalId();
        String estacionId = config.getEstacionId();

        for (int i = 0; i < cmbSucursal.getItems().size(); i++) {
            Sucursal s = cmbSucursal.getItems().get(i);
            if (s.getId().equals(sucursalId)) {
                cmbEstacion.getItems().addAll(s.getEstaciones());
                cmbSucursal.getSelectionModel().selectIndex(i);
                break;
            }
        }

        if (estacionId == null) {
            tglModulo.setSelected(true);
            cmbEstacion.setDisable(true);
        } else {
            for (int i = 0; i < cmbEstacion.getItems().size(); i++) {
                if (cmbEstacion.getItems().get(i).getId().equals(estacionId)) {
                    cmbEstacion.getSelectionModel().selectIndex(i);
                    break;
                }
            }
        }
    }
    
    private void mostrarError(String mensaje) {
        Controller controller = FlowController.getInstance().getController("AvisoView");
        if (controller instanceof AvisoController avisoController)
            avisoController.cambiarInformacionDeAviso(mensaje);
        FlowController.getInstance().goViewInWindowModal("AvisoView",
                FlowController.getInstance().getMainStage(), true);
    }

    private void mostrarExito(String mensaje) {
        Controller controller = FlowController.getInstance().getController("AvisoView");
        if (controller instanceof AvisoController avisoController)
            avisoController.cambiarInformacionDeAviso(mensaje);
        FlowController.getInstance().goViewInWindowModal("AvisoView",
                FlowController.getInstance().getMainStage(), true);
    }
}