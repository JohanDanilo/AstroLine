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

    // ── FXML ─────────────────────────────────────────────────────────────────
    @FXML
    private AnchorPane root;
    @FXML
    private MFXComboBox<Sucursal> cmbSucursal;
    @FXML
    private MFXComboBox<Estacion> cmbEstacion;
    @FXML
    private MFXCheckbox checkPreferencial;

    // ── Estado interno ────────────────────────────────────────────────────────
    private boolean cargandoConfig = false;

    // ── Listener nombrado (para poder removerlo en cada reload) ───────────────
    private ChangeListener<Estacion> listenerEstacion;

    // ── Initializable (JavaFX) ────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Solo se configura lo que no depende de datos: los converters de texto.
        // Los listeners se registran en initialize() para poder limpiarlos
        // correctamente en cada reload.
        configurarConverters();
    }

    // ── initialize() del FlowController ──────────────────────────────────────
    @Override
    public void initialize() {
        setNombreVista("Configuración local");
        limpiarEstadoInterno();          // ① limpiar estado y listener viejo
        cargarSucursales();
        configurarListeners();           // ② registrar listener nombrado fresco
        javafx.application.Platform.runLater(this::cargarConfiguracionActual);
    }

    // ── Limpieza de estado interno ────────────────────────────────────────────

    /**
     * Resetea la UI y remueve el listener de cmbEstacion antes de que se
     * configure la vista de nuevo. Se llama al inicio de initialize() para que
     * cada apertura/recarga parta de cero y no acumule listeners duplicados.
     */
    private void limpiarEstadoInterno() {
        // 1. Remover listener anterior para no acumularlo en cada reload
        if (listenerEstacion != null) {
            cmbEstacion.valueProperty().removeListener(listenerEstacion);
            listenerEstacion = null;
        }

        // 2. Limpiar selecciones y listas de los combos
        cmbSucursal.getSelectionModel().clearSelection();
        cmbSucursal.getItems().clear();
        cmbEstacion.getSelectionModel().clearSelection();
        cmbEstacion.getItems().clear();

        // 3. Resetear controles al estado inicial
        checkPreferencial.setSelected(false);
        cargandoConfig = false;
    }

    // ── Manejadores FXML ──────────────────────────────────────────────────────

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

    // ── Configuración de componentes ──────────────────────────────────────────

    /**
     * Configura los converters de texto de los combos. Se llama desde
     * initialize(URL, ResourceBundle) porque solo se necesita hacerlo una vez
     * y no depende de datos externos.
     */
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

    /**
     * Registra el listener de cmbEstacion como campo nombrado para poder
     * removerlo limpiamente en el próximo reload. Se llama desde initialize()
     * del FlowController, después de limpiarEstadoInterno().
     */
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

    // ── Carga de datos ────────────────────────────────────────────────────────

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

            // Sin configuración previa: deseleccionar todo limpiamente
            if (configuracion == null || configuracion.getSucursalId() == null) {
                seleccionarSucursal(null);
                cargarEstacionesDeSucursal(null);
                checkPreferencial.setSelected(false);
                return;
            }

            Sucursal sucursal = SucursalService.getInstancia()
                    .buscarSucursal(configuracion.getSucursalId());

            // La sucursal guardada ya no existe: limpiar igualmente
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
                seleccionarEstacion(estacion); // acepta null sin problema
            }
        } finally {
            cargandoConfig = false;
        }
    }

    /**
     * Selecciona una sucursal en cmbSucursal usando selectIndex para que MFX
     * sincronice correctamente su estado visual. Si sucursal es null, limpia
     * la selección.
     */
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

    /**
     * Selecciona una estación en cmbEstacion usando selectIndex para que MFX
     * sincronice correctamente su estado visual. Si estacion es null, limpia
     * la selección.
     */
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

    // ── Utilidades ────────────────────────────────────────────────────────────

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