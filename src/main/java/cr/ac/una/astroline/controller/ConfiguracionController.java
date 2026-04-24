package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.ConfiguracionLocal;
import cr.ac.una.astroline.model.Estacion;
import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.service.ConfiguracionService;
import cr.ac.una.astroline.service.SucursalService;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.Respuesta;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;

public class ConfiguracionController extends Controller implements Initializable {
    @FXML
    private AnchorPane root;
    @FXML
    private MFXComboBox<Sucursal> cmbSucursal;
    @FXML
    private MFXComboBox<Estacion> cmbEstacion;
    @FXML
    private MFXCheckbox checkPreferencial;

    private boolean cargandoConfig = false;

    private ChangeListener<Estacion> listenerEstacion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarConverters();
    }

    @Override
    public void initialize() {
        setNombreVista("Configuración local");
        limpiarEstadoInterno();
        cargarSucursales();
        configurarListeners();
        javafx.application.Platform.runLater(this::cargarConfiguracionActual);
    }

    private void limpiarEstadoInterno() {
        if (listenerEstacion != null) {
            cmbEstacion.valueProperty().removeListener(listenerEstacion);
            listenerEstacion = null;
        }

        cmbSucursal.getSelectionModel().clearSelection();
        cmbSucursal.getItems().clear();
        cmbEstacion.getSelectionModel().clearSelection();
        cmbEstacion.getItems().clear();
        checkPreferencial.setSelected(false);
        cargandoConfig = false;
    }

    @FXML
    private void onCmbSucursal(ActionEvent event) {
        if (cargandoConfig) {
            return;
        }
        cargarEstacionesDeSucursal(cmbSucursal.getValue());
    }

    @FXML
    private void onBtnGuardar(ActionEvent event) {
        Sucursal sucursal = cmbSucursal.getValue();
        if (sucursal == null) {
            mostrarAviso("Selecciona una sucursal antes de guardar.");
            return;
        }

        Estacion estacion = cmbEstacion.getValue();
        if (estacion == null) {
            mostrarAviso("Selecciona una estación antes de guardar.");
            return;
        }

        ConfiguracionLocal configActual = ConfiguracionService.getInstancia().getConfiguracion();
        boolean yaExisteConfig = configActual != null
                && configActual.getSucursalId() != null
                && !configActual.getSucursalId().isBlank();

        String titulo = yaExisteConfig ? "Sobrescribir configuración" : "Confirmar configuración";
        String mensaje = yaExisteConfig
                ? "Configuración actual:\n"
                + "  Sucursal : " + nombreSucursalGuardada(configActual) + "\n"
                + "  Estación : " + nombreEstacionGuardada(configActual) + "\n\n"
                + "Nueva configuración:\n"
                + "  Sucursal : " + sucursal.getNombre() + "\n"
                + "  Estación : " + estacion.getNombre() + "\n"
                + "  Preferencial: " + (checkPreferencial.isSelected() ? "Sí" : "No") + "\n\n"
                + "¿Confirmas el cambio?"
                : "¿Confirmas que este equipo operará como:\n\n"
                + "  Sucursal : " + sucursal.getNombre() + "\n"
                + "  Estación : " + estacion.getNombre() + "\n"
                + "  Preferencial: " + (checkPreferencial.isSelected() ? "Sí" : "No") + "?";

        if (!pedirConfirmacion(titulo, mensaje)) {
            return;
        }
        boolean preferencialNuevo = checkPreferencial.isSelected();
        if (estacion.isPreferencial() != preferencialNuevo) {
            estacion.setPreferencial(preferencialNuevo);
            Respuesta respuestaEstacion = SucursalService.getInstancia()
                    .actualizarEstacion(sucursal.getId(), estacion);

            if (!respuestaEstacion.getEstado()) {
                mostrarAviso("No se pudo actualizar el estado preferencial:\n"
                        + respuestaEstacion.getMensaje());
                return;
            }
        }

        try {
            Respuesta respuesta = ConfiguracionService.getInstancia()
                    .guardarConfiguracion(
                            sucursal.getId(),
                            estacion.getId(),
                            new ArrayList<>(estacion.getTramiteIds()),
                            checkPreferencial.isSelected()
                    );

            if (!respuesta.getEstado()) {
                mostrarAviso(respuesta.getMensaje());
                return;
            }

            mostrarAviso("Configuración guardada correctamente.");

        } catch (Exception e) {
            System.err.println("[ConfiguracionController] Error guardando configuracion.json: "
                    + e.getMessage());
            mostrarAviso("No se pudo guardar la configuración: " + e.getMessage());
        }
    }

    @FXML
    private void onBtnRestablecer(ActionEvent event) {
        cargarConfiguracionActual();
    }

    @FXML
    private void onCheckPreferencial(ActionEvent event) {
    }
    
    private void configurarConverters() {
        cmbSucursal.setConverter(new StringConverter<>() {
            @Override
            public String toString(Sucursal s) {
                return s == null ? "" : s.getNombre();
            }

            @Override
            public Sucursal fromString(String s) {
                return null;
            }
        });

        cmbEstacion.setConverter(new StringConverter<>() {
            @Override
            public String toString(Estacion e) {
                return e == null ? "" : e.getNombre();
            }

            @Override
            public Estacion fromString(String s) {
                return null;
            }
        });
    }

    private void configurarListeners() {
        listenerEstacion = (obs, vieja, nueva) -> {
            if (nueva != null) {
                checkPreferencial.setSelected(nueva.isPreferencial());
            } else {
                checkPreferencial.setSelected(false);
            }
        };
        cmbEstacion.valueProperty().addListener(listenerEstacion);
    }


    private void cargarSucursales() {
        cmbSucursal.getItems().setAll(SucursalService.getInstancia().getListaDeSucursales());
    }

    private void cargarEstacionesDeSucursal(Sucursal sucursal) {
        cmbEstacion.getSelectionModel().clearSelection();
        cmbEstacion.getItems().clear();
        if (sucursal != null) {
            cmbEstacion.getItems().addAll(sucursal.getEstaciones());
        }
    }

    private void cargarConfiguracionActual() {
        cargandoConfig = true;
        try {
            ConfiguracionLocal configuracion = ConfiguracionService.getInstancia().getConfiguracion();

            if (configuracion == null || configuracion.getSucursalId() == null) {
                seleccionarSucursal(null);
                cargarEstacionesDeSucursal(null);
                checkPreferencial.setSelected(false);
                return;
            }

            Sucursal sucursal = SucursalService.getInstancia().buscarSucursal(configuracion.getSucursalId());
            if (sucursal == null) {
                seleccionarSucursal(null);
                cargarEstacionesDeSucursal(null);
                checkPreferencial.setSelected(false);
                return;
            }

            seleccionarSucursal(sucursal);
            cargarEstacionesDeSucursal(sucursal);

            if (configuracion.getEstacionId() != null) {
                Estacion estacion = sucursal.buscarEstacion(configuracion.getEstacionId());
                if (estacion != null) {
                    cmbEstacion.setValue(estacion);
                }
                seleccionarEstacion(estacion);
            }
        } finally {
            cargandoConfig = false;
        }
    }

    private void seleccionarSucursal(Sucursal sucursal) {
        if (sucursal == null) {
            cmbSucursal.getSelectionModel().clearSelection();
            return;
        }
        int index = cmbSucursal.getItems().indexOf(sucursal);
        if (index >= 0) {
            cmbSucursal.getSelectionModel().selectIndex(index);
        }
    }
    
    private void seleccionarEstacion(Estacion estacion) {
        if (estacion == null) {
            cmbEstacion.getSelectionModel().clearSelection();
            return;
        }
        int index = cmbEstacion.getItems().indexOf(estacion);
        if (index >= 0) {
            cmbEstacion.getSelectionModel().selectIndex(index);
        }
    }
    private boolean pedirConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        alert.initOwner(getStage());

        Optional<ButtonType> resultado = alert.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.YES;
    }

    private void mostrarAviso(String mensaje) {
        Controller controller = FlowController.getInstance().getController("AvisoView");
        if (controller instanceof AvisoController avisoController) {
            avisoController.cambiarInformacionDeAviso(mensaje);
        }
        FlowController.getInstance().goViewInWindowModal(
                "AvisoView",
                getStage(),
                false
        );
    }

    private String nombreSucursalGuardada(ConfiguracionLocal config) {
        Sucursal s = SucursalService.getInstancia().buscarSucursal(config.getSucursalId());
        return s != null ? s.getNombre() : config.getSucursalId();
    }

    private String nombreEstacionGuardada(ConfiguracionLocal config) {
        if (config.getEstacionId() == null) {
            return "Kiosko";
        }
        Estacion e = SucursalService.getInstancia().buscarEstacion(config.getEstacionId());
        return e != null ? e.getNombre() : config.getEstacionId();
    }
}
