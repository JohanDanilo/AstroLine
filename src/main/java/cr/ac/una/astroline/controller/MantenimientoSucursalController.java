package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Estacion;
import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.model.Tramite;
import cr.ac.una.astroline.service.ConfiguracionService;
import cr.ac.una.astroline.service.SucursalService;
import cr.ac.una.astroline.service.TramiteService;
import cr.ac.una.astroline.util.AppContext;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.Respuesta;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;

public class MantenimientoSucursalController extends Controller implements Initializable {

    // ── FXML ─────────────────────────────────────────────────────────────────
    @FXML
    private AnchorPane root;
    @FXML
    private Label lblSucursal;
    @FXML
    private Label lblSucursal1;

    @FXML
    private MFXComboBox<Sucursal> cmbSucursal;
    @FXML
    private MFXTextField txtBarraBusquedaEstaciones;

    @FXML
    private Button btnEliminarSucursal;
    @FXML
    private Button btnAgregarEstacion;
    @FXML
    private Button btnEditarEstacion;
    @FXML
    private Button btnEliminarEstacion;

    @FXML
    private TableView<Estacion> tableEstaciones;
    @FXML
    private TableColumn<Estacion, String> colNombre;
    @FXML
    private TableColumn<Estacion, String> colId;
    @FXML
    private TableColumn<Estacion, String> colPreferencial;

    @FXML
    private TableView<Tramite> tableTramitesAsignados;
    @FXML
    private TableColumn<Tramite, String> colTramiteAsignado;
    @FXML
    private TableColumn<Tramite, String> colEstadoAsignado;

    @FXML
    private TableView<Tramite> tableTramitesDiponibles;
    @FXML
    private TableColumn<Tramite, String> colTramiteDisponible;
    @FXML
    private TableColumn<Tramite, String> colEstadoDisponible;

    private final SucursalService sucursalService = SucursalService.getInstancia();
    private final TramiteService tramiteService = TramiteService.getInstancia();
    private final ConfiguracionService configuracionService = ConfiguracionService.getInstancia();

    private Sucursal sucursalActual;
    private String sucursalActualId;
    private Estacion estacionSeleccionada;

    private final ObservableList<Estacion> estaciones = FXCollections.observableArrayList();
    private FilteredList<Estacion> estacionesFiltradas;

    private final ObservableList<Tramite> tramitesAsignados = FXCollections.observableArrayList();
    private final ObservableList<Tramite> tramitesDisponibles = FXCollections.observableArrayList();

    private static final DataFormat TRAMITE_FORMAT;

    static {
        DataFormat existing = DataFormat.lookupMimeType("application/astroline-tramite");
        TRAMITE_FORMAT = existing != null ? existing : new DataFormat("application/astroline-tramite");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarComboSucursales();
        configurarColumnasTablas();
        configurarTableEstaciones();
        configurarFiltro();
        configurarDragOver(tableTramitesAsignados);
        configurarDragOver(tableTramitesDiponibles);
        actualizarEstadoControles();
    }

    @Override
    public void initialize() {
        setNombreVista("Mantenimiento de sucursales");
        refrescarSucursales();
    }

    public void setSucursal(Sucursal sucursal) {
        sucursalActualId = sucursal != null ? sucursal.getId() : null;
        if (cmbSucursal != null) {
            seleccionarSucursal(buscarSucursalPorId(sucursalActualId));
        }
    }

    @FXML
    private void onActionCmbSucursal(ActionEvent event) {
        seleccionarSucursal(cmbSucursal.getValue());
    }

    @FXML
    private void onBtnAgregarSucursal(ActionEvent event) {
        agregarSucursal();
    }

    @FXML
    private void onBtnEditarSucursal(ActionEvent event) {
        if (sucursalActual == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccioná una sucursal primero.");
            return;
        }

        AppContext.getInstance().set("sucursalParaEditar", sucursalActual);

        FlowController.getInstance().goViewInWindowModal("RegistroSucursalView", getStage(), true);

        String idEditada = (String) AppContext.getInstance().get("ultimaSucursalId");
        AppContext.getInstance().delete("ultimaSucursalId");

        if (idEditada != null) {
            sucursalActualId = idEditada;
        }

        refrescarSucursales();
    }

    @FXML
    private void onBtnEiminarSucursal(ActionEvent event) {
        eliminarSucursal();
    }

    @FXML
    private void onBtnAgregarEstacion(ActionEvent event) {
        agregarEstacion();
    }

    @FXML
    private void onBtnEditarEstacion(ActionEvent event) {
        Estacion seleccionada = tableEstaciones.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccioná una estación primero.");
            return;
        }

        if (sucursalActual == null) {
            return;
        }

        AppContext.getInstance().set("sucursalParaEstacion", sucursalActual.getId());
        AppContext.getInstance().set("estacionParaEditar", seleccionada);

        FlowController.getInstance().goViewInWindowModal("RegistroEstacionView", getStage(), true);

        String idEditada = (String) AppContext.getInstance().get("ultimaEstacionAgregadaId");
        AppContext.getInstance().delete("ultimaEstacionAgregadaId");

        refrescarSucursales();

        if (idEditada != null) {
            seleccionarEstacionPorId(idEditada);
        }
    }

    @FXML
    private void onBtnEliminarEstacion(ActionEvent event) {
        eliminarEstacion();
    }

    private void onMousePressedEstaciones(MouseEvent event) {
        if (tableEstaciones.getSelectionModel().getSelectedItem() == null) {
            limpiarTramites();
            estacionSeleccionada = null;
            actualizarEstadoControles();
        }
    }

    private void onKeyPressedEstaciones(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE) {
            eliminarEstacion();
        }
    }

    @FXML
    private void onDragDetectedAsignados(MouseEvent event) {
        if (estacionSeleccionada == null) {
            return;
        }
        Tramite seleccionado = tableTramitesAsignados.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }

        Dragboard db = tableTramitesAsignados.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.put(TRAMITE_FORMAT, seleccionado.getId());
        db.setContent(content);
        event.consume();
    }

    @FXML
    private void onDragDroppedAsignados(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean exito = false;

        if (db.hasContent(TRAMITE_FORMAT) && estacionSeleccionada != null) {
            String tramiteId = (String) db.getContent(TRAMITE_FORMAT);
            Tramite tramite = tramiteService.buscarPorId(tramiteId);

            if (tramite != null && !estacionSeleccionada.atiendeTramite(tramiteId)) {
                tramitesDisponibles.remove(tramite);
                tramitesAsignados.add(tramite);

                Estacion copia = estacionSeleccionada.clonarEstacion(estacionSeleccionada);
                copia.agregarTramite(tramiteId);

                sucursalService.actualizarEstacion(sucursalActual.getId(), copia);

                estacionSeleccionada = copia;

                sincronizarConfiguracionSiCorresponde(copia);

                tableTramitesAsignados.refresh();
                tableTramitesDiponibles.refresh();
                exito = true;
            }
        }
        event.setDropCompleted(exito);
        event.consume();
    }

    @FXML
    private void onDragDoneAsignados(DragEvent event) {
        tableTramitesAsignados.getStyleClass().remove("drag-over");
        event.consume();
    }

    @FXML
    private void onDragEnteredAsignados(DragEvent event) {
        if (event.getDragboard().hasContent(TRAMITE_FORMAT) && estacionSeleccionada != null) {
            if (!tableTramitesAsignados.getStyleClass().contains("drag-over")) {
                tableTramitesAsignados.getStyleClass().add("drag-over");
            }
        }
        event.consume();
    }

    @FXML
    private void onDragDetectedDisponibles(MouseEvent event) {
        if (estacionSeleccionada == null) {
            return;
        }
        Tramite seleccionado = tableTramitesDiponibles.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }

        Dragboard db = tableTramitesDiponibles.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.put(TRAMITE_FORMAT, seleccionado.getId());
        db.setContent(content);
        event.consume();
    }

    @FXML
    private void onDragDroppedDisponibles(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean exito = false;

        if (db.hasContent(TRAMITE_FORMAT) && estacionSeleccionada != null) {
            String tramiteId = (String) db.getContent(TRAMITE_FORMAT);
            Tramite tramite = tramiteService.buscarPorId(tramiteId);

            if (tramite != null && estacionSeleccionada.atiendeTramite(tramiteId)) {

                tramitesAsignados.remove(tramite);
                tramitesDisponibles.add(tramite);

                Estacion copia = estacionSeleccionada.clonarEstacion(estacionSeleccionada);
                copia.quitarTramite(tramiteId);

                sucursalService.actualizarEstacion(sucursalActual.getId(), copia);

                estacionSeleccionada = copia;

                sincronizarConfiguracionSiCorresponde(copia);

                tableTramitesAsignados.refresh();
                tableTramitesDiponibles.refresh();
                exito = true;
            }
        }
        event.setDropCompleted(exito);
        event.consume();
    }

    @FXML
    private void onDragDoneDisponibles(DragEvent event) {
        tableTramitesDiponibles.getStyleClass().remove("drag-over");
        event.consume();
    }

    @FXML
    private void onDragEntered(DragEvent event) {
        if (event.getDragboard().hasContent(TRAMITE_FORMAT) && estacionSeleccionada != null) {
            if (!tableTramitesDiponibles.getStyleClass().contains("drag-over")) {
                tableTramitesDiponibles.getStyleClass().add("drag-over");
            }
        }
        event.consume();
    }

    private void agregarSucursal() {

        AppContext.getInstance().set("sucursalParaEditar", null);

        FlowController.getInstance().goViewInWindowModal("RegistroSucursalView", getStage(), true);

        String nuevaId = (String) AppContext.getInstance().get("ultimaSucursalId");
        AppContext.getInstance().delete("ultimaSucursalId");

        if (nuevaId != null) {
            sucursalActualId = nuevaId;
        }
        txtBarraBusquedaEstaciones.clear();
        refrescarSucursales();
    }

    private void eliminarSucursal() {
        if (sucursalActual == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccioná una sucursal primero.");
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Se eliminará la sucursal \"" + sucursalActual.getNombre()
                + "\" con todas sus estaciones.\nEsta acción no se puede deshacer.",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(null);

        confirmacion.showAndWait().ifPresent(tipo -> {
            if (tipo != ButtonType.YES) {
                return;
            }

            String idEliminada = sucursalActual.getId();
            Respuesta respuesta = sucursalService.eliminarSucursal(idEliminada);
            if (!respuesta.getEstado()) {
                mostrarAlerta(Alert.AlertType.ERROR, respuesta.getMensaje());
                return;
            }

            if (idEliminada.equals(configuracionService.getSucursalId())) {
                configuracionService.resetearConfiguracion();
            }

            if (idEliminada.equals(sucursalActualId)) {
                sucursalActualId = null;
            }
            txtBarraBusquedaEstaciones.clear();
            refrescarSucursales();
        });
    }

    private void agregarEstacion() {
        if (sucursalActual == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccioná o creá una sucursal primero.");
            return;
        }

        AppContext.getInstance().set("sucursalParaEstacion", sucursalActual.getId());
        AppContext.getInstance().set("estacionParaEditar", null);

        FlowController.getInstance().goViewInWindowModal(
                "RegistroEstacionView", getStage(), true);

        String nuevaId = (String) AppContext.getInstance().get("ultimaEstacionAgregadaId");
        AppContext.getInstance().delete("ultimaEstacionAgregadaId");

        refrescarSucursales();
        if (nuevaId != null) {
            seleccionarEstacionPorId(nuevaId);
        }
    }

    private void eliminarEstacion() {
        Estacion seleccionada = tableEstaciones.getSelectionModel().getSelectedItem();
        if (seleccionada == null || sucursalActual == null) {
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Desea eliminar la estación \"" + seleccionada.getNombre()
                + "\"?\nEsta acción no se puede deshacer.",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(null);

        confirmacion.showAndWait().ifPresent(tipo -> {
            if (tipo != ButtonType.YES) {
                return;
            }

            Respuesta respuesta = sucursalService.eliminarEstacion(sucursalActual.getId(), seleccionada.getId());

            if (!respuesta.getEstado()) {
                mostrarAlerta(Alert.AlertType.ERROR, respuesta.getMensaje());
                return;
            }

            if (seleccionada.getId().equals(configuracionService.getEstacionId())) {
                configuracionService.resetearConfiguracion();
            }

            refrescarSucursales();
        });
    }

    private void configurarComboSucursales() {
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
    }

    private void configurarColumnasTablas() {
        colTramiteAsignado.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().getNombre()));
        colEstadoAsignado.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().isActivo() ? "Activo" : "Inactivo"));

        colTramiteDisponible.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().getNombre()));
        colEstadoDisponible.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().isActivo() ? "Activo" : "Inactivo"));

        tableTramitesAsignados.setItems(tramitesAsignados);
        tableTramitesDiponibles.setItems(tramitesDisponibles);
    }

    private void configurarTableEstaciones() {
        colNombre.setCellValueFactory(d -> {
            Estacion e = d.getValue();
            String sufEstado = e.isEstaActiva() ? "" : "  [Inactiva]";
            return new SimpleStringProperty(e.getNombre() + sufEstado);
        });
        colId.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().getId()));
        colPreferencial.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().isPreferencial() ? "★" : ""));

        estacionesFiltradas = new FilteredList<>(estaciones, e -> true);
        tableEstaciones.setItems(estacionesFiltradas);

        tableEstaciones.getSelectionModel().selectedItemProperty().addListener(
                (obs, ant, nueva) -> cargarTramitesDeEstacion(nueva));

        tableEstaciones.setOnMousePressed(this::onMousePressedEstaciones);
        tableEstaciones.setOnKeyPressed(this::onKeyPressedEstaciones);
    }

    private void configurarFiltro() {
        txtBarraBusquedaEstaciones.textProperty().addListener((obs, ant, nuevo) -> {
            String criterio = nuevo == null ? "" : nuevo.trim().toLowerCase();
            estacionesFiltradas.setPredicate(e
                    -> criterio.isBlank() || (e.getNombre() != null
                    && e.getNombre().toLowerCase().contains(criterio)));
        });
    }

    private void configurarDragOver(TableView<?> tabla) {
        tabla.setOnDragOver(event -> {
            if (event.getDragboard().hasContent(TRAMITE_FORMAT)
                    && event.getGestureSource() != tabla
                    && estacionSeleccionada != null) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        tabla.setOnDragExited(event -> {
            tabla.getStyleClass().remove("drag-over");
            event.consume();
        });
    }

    private void refrescarSucursales() {
        Sucursal seleccionada = buscarSucursalPorId(sucursalActualId);

        cmbSucursal.getSelectionModel().clearSelection();
        cmbSucursal.getItems().setAll(sucursalService.getListaDeSucursales());

        if (seleccionada == null && !cmbSucursal.getItems().isEmpty()) {
            seleccionada = cmbSucursal.getItems().get(0);
        }

        seleccionarSucursal(seleccionada);
    }

    private void seleccionarSucursal(Sucursal sucursal) {
        if (cmbSucursal.getValue() != sucursal) {
            cmbSucursal.setValue(sucursal);
        }
        aplicarSucursalSeleccionada(sucursal);
    }

    private void aplicarSucursalSeleccionada(Sucursal sucursal) {
        sucursalActual = sucursal;
        sucursalActualId = sucursal != null ? sucursal.getId() : null;

        if (sucursalActual == null) {
            limpiarVistaSinSucursal();
            return;
        }

        recargarEstaciones();
        actualizarEncabezadoSucursal();
    }

    private void recargarEstaciones() {
        if (sucursalActualId != null) {
            sucursalActual = buscarSucursalPorId(sucursalActualId);
        }
        if (sucursalActual == null) {
            limpiarVistaSinSucursal();
            return;
        }

        String estacionIdSeleccionada = estacionSeleccionada != null
                ? estacionSeleccionada.getId() : null;

        estaciones.setAll(sucursalActual.getEstaciones());
        actualizarEncabezadoSucursal();

        if (estacionIdSeleccionada != null && seleccionarEstacionPorId(estacionIdSeleccionada)) {
            actualizarEstadoControles();
            return;
        }

        if (!estaciones.isEmpty()) {
            tableEstaciones.getSelectionModel().select(estaciones.get(0));
            return;
        }

        tableEstaciones.getSelectionModel().clearSelection();
        estacionSeleccionada = null;
        limpiarTramites();
        actualizarEstadoControles();
    }

    private void cargarTramitesDeEstacion(Estacion estacion) {
        estacionSeleccionada = estacion;
        limpiarTramites();

        if (estacion == null) {
            actualizarEstadoControles();
            return;
        }

        for (Tramite t : tramiteService.getListaDeTramites()) {
            if (estacion.atiendeTramite(t.getId())) {
                tramitesAsignados.add(t);
            } else {
                tramitesDisponibles.add(t);
            }
        }

        actualizarEstadoControles();
    }

    private void limpiarVistaSinSucursal() {
        if (cmbSucursal.getValue() != null) {
            cmbSucursal.setValue(null);
        }
        lblSucursal.setText("No hay sucursales registradas.");
        tableEstaciones.getSelectionModel().clearSelection();
        estaciones.clear();
        estacionSeleccionada = null;
        limpiarTramites();
        actualizarEstadoControles();
    }

    private void limpiarTramites() {
        tramitesAsignados.clear();
        tramitesDisponibles.clear();
    }

    private void actualizarEstadoControles() {
        boolean haySucursal = sucursalActual != null;
        boolean hayEstacion = estacionSeleccionada != null;

        txtBarraBusquedaEstaciones.setDisable(!haySucursal);
        tableEstaciones.setDisable(!haySucursal);
        btnAgregarEstacion.setDisable(!haySucursal);
        btnEliminarSucursal.setDisable(!haySucursal);
        btnEditarEstacion.setDisable(!hayEstacion);
        btnEliminarEstacion.setDisable(!hayEstacion);
        tableTramitesAsignados.setDisable(!hayEstacion);
        tableTramitesDiponibles.setDisable(!hayEstacion);
    }

    private void actualizarEncabezadoSucursal() {
        if (sucursalActual == null) {
            lblSucursal.setText("No hay sucursales registradas.");
            return;
        }
        int n = sucursalActual.getEstaciones().size();
        lblSucursal.setText("Sucursal: " + sucursalActual.getNombre()
                + "  |  " + n + (n == 1 ? " estación" : " estaciones"));
    }

    private Sucursal buscarSucursalPorId(String id) {
        return id == null ? null : sucursalService.buscarSucursal(id);
    }

    private boolean seleccionarEstacionPorId(String estacionId) {
        if (estacionId == null) {
            return false;
        }
        for (Estacion e : estaciones) {
            if (estacionId.equals(e.getId())) {
                tableEstaciones.getSelectionModel().select(e);
                tableEstaciones.scrollTo(e);
                return true;
            }
        }
        return false;
    }

    private void sincronizarConfiguracionSiCorresponde(Estacion estacionModificada) {
        String estacionConfigurada = configuracionService.getEstacionId();
        if (estacionModificada == null || estacionConfigurada == null) {
            return;
        }
        if (!estacionModificada.getId().equals(estacionConfigurada)) {
            return;
        }

        Respuesta respuesta = configuracionService.guardarConfiguracion(
                configuracionService.getSucursalId(),
                estacionModificada.getId(),
                new java.util.ArrayList<>(estacionModificada.getTramiteIds()),
                estacionModificada.isPreferencial()
        );

        if (!respuesta.getEstado()) {
            System.err.println("[MantenimientoSucursalController] "
                    + "No se pudo sincronizar configuracion.json: "
                    + respuesta.getMensaje());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
