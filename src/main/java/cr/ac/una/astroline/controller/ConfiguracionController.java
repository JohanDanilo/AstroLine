package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.ConfiguracionLocal;
import cr.ac.una.astroline.model.Estacion;
import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.service.ConfiguracionService;
import cr.ac.una.astroline.service.SucursalService;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.GsonUtil;
import cr.ac.una.astroline.util.Respuesta;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;

/**
 * Controlador para la configuración local de este equipo.
 *
 * Responsabilidades:
 *  - Mostrar y restaurar la configuración guardada en configuracion.json.
 *  - Pedir confirmación antes de guardar (diferente mensaje si ya existía config).
 *  - Si el usuario cambió el flag preferencial de la estación, propagarlo
 *    a sucursales.json vía SucursalService (que dispara sync P2P automáticamente).
 *  - Guardar sucursalId + estacionId en configuracion.json (solo esta máquina,
 *    sin propagación P2P porque la config es por equipo).
 *
 * @author JohanDanilo
 */
public class ConfiguracionController extends Controller implements Initializable {

    @FXML private AnchorPane root;
    @FXML private MFXComboBox<Sucursal> cmbSucursal;
    @FXML private MFXComboBox<Estacion> cmbEstacion;
    @FXML private MFXCheckbox checkPreferencial;

    /**
     * Flag que bloquea onCmbSucursal durante el binding programático.
     * MFXComboBox dispara onAction al llamar setValue() por código,
     * lo que causaría una doble ejecución de cargarEstacionesDeSucursal
     * y un setValue(null) en cmbEstacion en medio del flujo de carga.
     */
    private boolean cargandoConfig = false;

    /**
     * Llamado UNA SOLA VEZ por FXMLLoader al cargar el FXML.
     * Aquí van los listeners y converters: el FlowController cachea el
     * controller y reutiliza la misma instancia. Si estos se pusieran en
     * el initialize() de no-args, se acumularían con cada apertura de ventana.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarConverters();
        configurarListeners();
    }

    /**
     * Llamado por FlowController cada vez que la ventana se abre.
     * Solo recarga datos frescos — no añade listeners ni converters.
     */
    @Override
    public void initialize() {
        setNombreVista("Configuración local");
        cargarSucursales();
        // Platform.runLater difiere el setValue() hasta después del primer ciclo
        // de render: MFXComboBox no actualiza la vista si setValue() se llama
        // antes de que el Stage esté completamente desplegado.
        javafx.application.Platform.runLater(this::cargarConfiguracionActual);
    }

    // ── Handlers FXML ─────────────────────────────────────────────────────────

    @FXML
    private void onCmbSucursal(ActionEvent event) {
        // Ignorar disparos programáticos durante cargarConfiguracionActual.
        // MFXComboBox dispara onAction incluso cuando setValue() se llama por código.
        if (cargandoConfig) return;
        cargarEstacionesDeSucursal(cmbSucursal.getValue());
    }

    @FXML
    private void onBtnGuardar(ActionEvent event) {

        // ── 1. Obtener selecciones de los combos ──────────────────────────────
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

        // ── 2. Confirmación ───────────────────────────────────────────────────
        ConfiguracionLocal configActual = ConfiguracionService.getInstancia().getConfiguracion();
        boolean yaExisteConfig = configActual != null
                && configActual.getSucursalId() != null
                && !configActual.getSucursalId().isBlank();

        String titulo  = yaExisteConfig ? "Sobrescribir configuración" : "Confirmar configuración";
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

        if (!pedirConfirmacion(titulo, mensaje)) return;

        // ── 3. Actualizar flag preferencial de la estación en sucursales.json ─
        //       Es lo ÚNICO editable de una estación desde esta vista.
        //       Solo se propaga si realmente cambió — evita guardar sin necesidad.
        boolean prefencialNuevo = checkPreferencial.isSelected();
        if (estacion.isPreferencial() != prefencialNuevo) {
            estacion.setPreferencial(prefencialNuevo);
            Respuesta respuestaEstacion = SucursalService.getInstancia()
                    .actualizarEstacion(sucursal.getId(), estacion);

            if (!respuestaEstacion.getEstado()) {
                mostrarAviso("No se pudo actualizar el estado preferencial:\n"
                        + respuestaEstacion.getMensaje());
                return; // no continuar si sucursales.json no se pudo guardar
            }
        }

        // ── 4. Escribir configuracion.json directamente ───────────────────────
        //       Sin pasar por ConfiguracionService para eliminar cualquier
        //       punto de fallo intermedio. GsonUtil.guardar() (no guardarYPropagar)
        //       garantiza que este archivo NUNCA se propague por P2P.
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

    /**
     * Restablece el formulario al estado guardado actualmente en configuracion.json,
     * descartando cualquier cambio sin guardar hecho en la sesión actual.
     */
    @FXML
    private void onBtnRestablecer(ActionEvent event) {
        cargarConfiguracionActual();
    }

    /**
     * El checkbox es editable: el usuario puede cambiar el flag preferencial
     * de la estación seleccionada directamente desde esta pantalla.
     * El cambio se aplica a sucursales.json al presionar Guardar.
     * No se necesita lógica adicional aquí.
     */
    @FXML
    private void onCheckPreferencial(ActionEvent event) { }

    // ── Lógica privada ────────────────────────────────────────────────────────

    private void configurarConverters() {
        cmbSucursal.setConverter(new StringConverter<>() {
            @Override
            public String toString(Sucursal s) { return s == null ? "" : s.getNombre(); }
            @Override
            public Sucursal fromString(String s) { return null; }
        });

        cmbEstacion.setConverter(new StringConverter<>() {
            @Override
            public String toString(Estacion e) { return e == null ? "" : e.getNombre(); }
            @Override
            public Estacion fromString(String s) { return null; }
        });
    }

    /**
     * Cuando el usuario selecciona una estación, el checkbox refleja
     * automáticamente su flag preferencial actual.
     */
    private void configurarListeners() {
        cmbEstacion.valueProperty().addListener((obs, vieja, nueva) -> {
            if (nueva != null) {
                checkPreferencial.setSelected(nueva.isPreferencial());
            } else {
                checkPreferencial.setSelected(false);
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
    }

    /**
     * Restaura el formulario a partir de lo que está guardado en configuracion.json.
     * Si no hay configuración previa, limpia todos los controles.
     * Este método es usado tanto en initialize() como en onBtnRestablecer().
     */
    private void cargarConfiguracionActual() {
        cargandoConfig = true;
        try {
            ConfiguracionLocal configuracion = ConfiguracionService.getInstancia().getConfiguracion();

            if (configuracion == null || configuracion.getSucursalId() == null) {
                // Sin configuración previa: limpiar todo
                cmbSucursal.setValue(null);
                cmbEstacion.getItems().clear();
                cmbEstacion.setValue(null);
                checkPreferencial.setSelected(false);
                return;
            }

            Sucursal sucursal = SucursalService.getInstancia()
                    .buscarSucursal(configuracion.getSucursalId());
            if (sucursal == null) return;

            // Seleccionar sucursal: onCmbSucursal queda bloqueado por el flag,
            // así que cargamos las estaciones explícitamente nosotros.
            cmbSucursal.setValue(sucursal);
            cargarEstacionesDeSucursal(sucursal);

            // Restaurar la estación guardada (si sigue existiendo)
            if (configuracion.getEstacionId() != null) {
                Estacion estacion = sucursal.buscarEstacion(configuracion.getEstacionId());
                if (estacion != null) {
                    cmbEstacion.setValue(estacion);
                    // El listener de valueProperty ya actualiza checkPreferencial
                }
            }
        } finally {
            cargandoConfig = false;
        }
    }

    // ── Diálogos ──────────────────────────────────────────────────────────────

    /**
     * Muestra un Alert de confirmación con botones Sí / No.
     *
     * @return true si el usuario presionó Sí
     */
    private boolean pedirConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        alert.initOwner(getStage()); // bloquea esta ventana, no el mainStage

        Optional<ButtonType> resultado = alert.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.YES;
    }

    /**
     * Muestra el modal AvisoView con el mensaje indicado.
     * Usa getStage() como owner para que el aviso bloquee esta ventana
     * y no el mainStage principal.
     */
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

    // ── Helpers de texto para el diálogo de sobrescritura ────────────────────

    /**
     * Retorna el nombre de la sucursal guardada actualmente.
     * Si ya no existe en la lista, muestra su id como fallback.
     */
    private String nombreSucursalGuardada(ConfiguracionLocal config) {
        Sucursal s = SucursalService.getInstancia().buscarSucursal(config.getSucursalId());
        return s != null ? s.getNombre() : config.getSucursalId();
    }

    /**
     * Retorna el nombre de la estación guardada actualmente.
     * Si ya no existe en la lista, muestra su id como fallback.
     */
    private String nombreEstacionGuardada(ConfiguracionLocal config) {
        if (config.getEstacionId() == null) return "Kiosko";
        Estacion e = SucursalService.getInstancia().buscarEstacion(config.getEstacionId());
        return e != null ? e.getNombre() : config.getEstacionId();
    }
}