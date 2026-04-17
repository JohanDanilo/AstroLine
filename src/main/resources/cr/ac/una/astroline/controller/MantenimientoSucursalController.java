package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Estacion;
import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.model.Tramite;
import cr.ac.una.astroline.service.SucursalService;
import cr.ac.una.astroline.service.TramiteService;
import cr.ac.una.astroline.util.Respuesta;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
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

/**
 * Controlador para el mantenimiento de estaciones de una sucursal.
 *
 * Responsabilidades:
 *   - Listar y filtrar las estaciones de la sucursal activa.
 *   - Crear y eliminar estaciones.
 *   - Asignar/desasignar trámites a una estación mediante drag & drop
 *     entre las tablas "Trámites Asignados" y "Trámites Disponibles".
 *   - Persistir cambios vía SucursalService (que propaga al resto de peers).
 *
 * Uso: llamar a {@link #setSucursal(Sucursal)} inmediatamente después de
 * cargar la vista desde el controlador padre.
 *
 * @author JohanDanilo
 */
public class MantenimientoSucursalController extends Controller implements Initializable {

    // ── FXML ─────────────────────────────────────────────────────────────────

    @FXML private Label              lblSucursal;
    @FXML private MFXTextField       txtBarraBusquedaEstaciones;
    @FXML private MFXButton          btnAgregar;

    @FXML private ListView<Estacion> listaEstaciones;

    @FXML private TableView<Tramite>          tableTramitesAsignados;
    @FXML private TableColumn<Tramite, String> colTramiteAsignado;
    @FXML private TableColumn<Tramite, String> colEstadoAsignado;

    @FXML private TableView<Tramite>          tableTramitesDiponibles;   // typo intencional del FXML
    @FXML private TableColumn<Tramite, String> colTramiteDisponible;
    @FXML private TableColumn<Tramite, String> colEstadoDisponible;

    // ── Servicios ─────────────────────────────────────────────────────────────

    private final SucursalService sucursalService = SucursalService.getInstancia();
    private final TramiteService  tramiteService  = TramiteService.getInstancia();

    // ── Estado del controlador ────────────────────────────────────────────────

    private Sucursal sucursalActual;
    private Estacion estacionSeleccionada;

    private final ObservableList<Estacion> estaciones        = FXCollections.observableArrayList();
    private       FilteredList<Estacion>   estacionesFiltradas;

    private final ObservableList<Tramite>  tramitesAsignados  = FXCollections.observableArrayList();
    private final ObservableList<Tramite>  tramitesDisponibles = FXCollections.observableArrayList();

    /**
     * DataFormat personalizado para el drag & drop de trámites.
     * El bloque estático evita IllegalArgumentException si el controlador
     * se instancia más de una vez en la misma sesión (JavaFX reutiliza el registro).
     */
    private static final DataFormat TRAMITE_FORMAT;
    static {
        DataFormat existing = DataFormat.lookupMimeType("application/astroline-tramite");
        TRAMITE_FORMAT = (existing != null) ? existing
                                            : new DataFormat("application/astroline-tramite");
    }

    // ── Inicialización ────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnasTablas();
        configurarListView();
        configurarFiltro();
        configurarDragOver(tableTramitesAsignados);
        configurarDragOver(tableTramitesDiponibles);
    }

    /** Configura cell value factories de las cuatro columnas. */
    private void configurarColumnasTablas() {
        colTramiteAsignado.setCellValueFactory(
                cd -> new SimpleStringProperty(cd.getValue().getNombre()));
        colEstadoAsignado.setCellValueFactory(
                cd -> new SimpleStringProperty(cd.getValue().isActivo() ? "Activo" : "Inactivo"));

        colTramiteDisponible.setCellValueFactory(
                cd -> new SimpleStringProperty(cd.getValue().getNombre()));
        colEstadoDisponible.setCellValueFactory(
                cd -> new SimpleStringProperty(cd.getValue().isActivo() ? "Activo" : "Inactivo"));

        tableTramitesAsignados.setItems(tramitesAsignados);
        tableTramitesDiponibles.setItems(tramitesDisponibles);
    }

    /** Configura el ListView: cell factory + listener de selección. */
    private void configurarListView() {
        estacionesFiltradas = new FilteredList<>(estaciones, e -> true);
        listaEstaciones.setItems(estacionesFiltradas);

        listaEstaciones.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Estacion e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null : e.getNombre()
                        + (e.isPreferencial() ? "  ★" : "")
                        + (e.isEstaActiva()   ? "" : "  [Inactiva]"));
            }
        });

        listaEstaciones.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, nueva) -> cargarTramitesDeEstacion(nueva));
    }

    /** Conecta el campo de búsqueda con el predicado del FilteredList. */
    private void configurarFiltro() {
        txtBarraBusquedaEstaciones.textProperty().addListener((obs, anterior, nuevo) -> {
            String lower = nuevo == null ? "" : nuevo.toLowerCase().trim();
            estacionesFiltradas.setPredicate(e ->
                    lower.isEmpty() || e.getNombre().toLowerCase().contains(lower));
        });
    }

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Recibe la sucursal cuyas estaciones se van a gestionar.
     * Debe llamarse justo después de cargar la vista desde el controlador padre.
     *
     * @param sucursal la sucursal a mantener
     */
    public void setSucursal(Sucursal sucursal) {
        this.sucursalActual = sucursal;
        lblSucursal.setText("Sucursal:  " + sucursal.getNombre());
        recargarEstaciones();
    }

    // ── Carga de datos ────────────────────────────────────────────────────────

    /** Recarga la lista lateral de estaciones desde el modelo vivo de la sucursal. */
    private void recargarEstaciones() {
        Estacion selAnterior = listaEstaciones.getSelectionModel().getSelectedItem();
        estaciones.setAll(sucursalActual.getEstaciones());

        // Restaurar selección si la estación sigue existiendo
        if (selAnterior != null) {
            estaciones.stream()
                      .filter(e -> e.getId().equals(selAnterior.getId()))
                      .findFirst()
                      .ifPresent(e -> listaEstaciones.getSelectionModel().select(e));
        }
    }

    /**
     * Carga los trámites de la estación seleccionada en las dos tablas.
     * Trámites asignados = IDs presentes en estacion.tramiteIds.
     * Trámites disponibles = el resto de la lista global de trámites.
     */
    private void cargarTramitesDeEstacion(Estacion estacion) {
        estacionSeleccionada = estacion;
        tramitesAsignados.clear();
        tramitesDisponibles.clear();

        if (estacion == null) return;

        for (Tramite t : tramiteService.getListaDeTramites()) {
            if (estacion.atiendeTramite(t.getId())) {
                tramitesAsignados.add(t);
            } else {
                tramitesDisponibles.add(t);
            }
        }
    }

    // ── Handler botón Agregar ─────────────────────────────────────────────────

    @FXML
    private void onActionBtnAgregar() {
        if (sucursalActual == null) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nueva Estación");
        dialog.setHeaderText("Agregar estación en " + sucursalActual.getNombre());
        dialog.setContentText("Nombre de la estación:");

        dialog.showAndWait().ifPresent(nombre -> {
            nombre = nombre.trim();
            if (nombre.isBlank()) {
                mostrarAlerta(Alert.AlertType.WARNING, "El nombre no puede estar vacío.");
                return;
            }

            String id   = sucursalService.generarIdEstacion();
            Estacion nueva = new Estacion(id, nombre, sucursalActual.getId(), false, true);

            Respuesta r = sucursalService.agregarEstacion(sucursalActual.getId(), nueva);
            if (r.getEstado()) {
                recargarEstaciones();
                // Seleccionar la nueva estación automáticamente
                estaciones.stream()
                           .filter(e -> e.getId().equals(id))
                           .findFirst()
                           .ifPresent(e -> listaEstaciones.getSelectionModel().select(e));
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, r.getMensaje());
            }
        });
    }

    // ── Handlers del ListView ─────────────────────────────────────────────────

    @FXML
    private void onMousePressedEstaciones(MouseEvent event) {
        // La selección la maneja el listener del selectedItemProperty.
        // Si se hace clic en un área vacía, deseleccionar limpiando las tablas.
        if (listaEstaciones.getSelectionModel().getSelectedItem() == null) {
            tramitesAsignados.clear();
            tramitesDisponibles.clear();
            estacionSeleccionada = null;
        }
    }

    @FXML
    private void onKeyPressedEstaciones(KeyEvent event) {
        if (event.getCode() != KeyCode.DELETE) return;

        Estacion seleccionada = listaEstaciones.getSelectionModel().getSelectedItem();
        if (seleccionada == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Desea eliminar la estación \"" + seleccionada.getNombre() + "\"?\n"
                + "Esta acción no se puede deshacer.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(tipo -> {
            if (tipo != ButtonType.YES) return;

            Respuesta r = sucursalService.eliminarEstacion(
                    sucursalActual.getId(), seleccionada.getId());
            if (r.getEstado()) {
                recargarEstaciones();
                tramitesAsignados.clear();
                tramitesDisponibles.clear();
                estacionSeleccionada = null;
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, r.getMensaje());
            }
        });
    }

    // ── Drag & Drop — tabla Trámites Asignados ────────────────────────────────

    /**
     * Inicio de arrastre desde la tabla de asignados.
     * Pone el ID del trámite en el Dragboard para que la tabla de disponibles
     * sepa qué quitar cuando se suelte.
     */
    @FXML
    private void onDragDetectedAsignados(MouseEvent event) {
        if (estacionSeleccionada == null) return;
        Tramite seleccionado = tableTramitesAsignados.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        Dragboard db = tableTramitesAsignados.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.put(TRAMITE_FORMAT, seleccionado.getId());
        db.setContent(content);
        event.consume();
    }

    /**
     * Drop sobre la tabla de asignados.
     * Solo acepta trámites que aún NO están asignados a la estación
     * (evita duplicados si el usuario arrastra dentro de la misma tabla).
     */
    @FXML
    private void onDragDroppedAsignados(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean exito = false;

        if (db.hasContent(TRAMITE_FORMAT) && estacionSeleccionada != null) {
            String tramiteId = (String) db.getContent(TRAMITE_FORMAT);

            // Solo procesar si viene de la tabla de disponibles
            if (!estacionSeleccionada.atiendeTramite(tramiteId)) {
                boolean agregado = estacionSeleccionada.agregarTramite(tramiteId);
                if (agregado) {
                    sucursalService.actualizarEstacion(sucursalActual.getId(), estacionSeleccionada);
                    cargarTramitesDeEstacion(estacionSeleccionada);
                    exito = true;
                }
            }
        }

        event.setDropCompleted(exito);
        event.consume();
    }

    @FXML
    private void onDragDoneAsignados(DragEvent event) {
        // Limpiar highlight residual
        tableTramitesAsignados.getStyleClass().remove("drag-over");
        event.consume();
    }

    /** Resalta la tabla de asignados cuando el cursor entra con un trámite. */
    @FXML
    private void onDragEnteredAsignados(DragEvent event) {
        if (event.getDragboard().hasContent(TRAMITE_FORMAT)
                && estacionSeleccionada != null) {
            tableTramitesAsignados.getStyleClass().add("drag-over");
        }
        event.consume();
    }

    // ── Drag & Drop — tabla Trámites Disponibles ──────────────────────────────

    /**
     * Inicio de arrastre desde la tabla de disponibles.
     * Pone el ID del trámite en el Dragboard para que la tabla de asignados
     * sepa qué agregar cuando se suelte.
     */
    @FXML
    private void onDragDetectedDisponibles(MouseEvent event) {
        if (estacionSeleccionada == null) return;
        Tramite seleccionado = tableTramitesDiponibles.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        Dragboard db = tableTramitesDiponibles.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.put(TRAMITE_FORMAT, seleccionado.getId());
        db.setContent(content);
        event.consume();
    }

    /**
     * Drop sobre la tabla de disponibles.
     * Solo acepta trámites que SÍ están asignados actualmente
     * (los desasigna de la estación).
     */
    @FXML
    private void onDragDroppedDisponibles(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean exito = false;

        if (db.hasContent(TRAMITE_FORMAT) && estacionSeleccionada != null) {
            String tramiteId = (String) db.getContent(TRAMITE_FORMAT);

            // Solo procesar si el trámite efectivamente está asignado
            if (estacionSeleccionada.atiendeTramite(tramiteId)) {
                boolean quitado = estacionSeleccionada.quitarTramite(tramiteId);
                if (quitado) {
                    sucursalService.actualizarEstacion(sucursalActual.getId(), estacionSeleccionada);
                    cargarTramitesDeEstacion(estacionSeleccionada);
                    exito = true;
                }
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

    /**
     * Resalta la tabla de disponibles cuando el cursor entra con un trámite.
     * NOTA: en el FXML este handler está declarado como onDragEntered (sin sufijo)
     * porque takka_sama lo asignó a tableTramitesDiponibles con ese nombre exacto.
     */
    @FXML
    private void onDragEntered(DragEvent event) {
        if (event.getDragboard().hasContent(TRAMITE_FORMAT)
                && estacionSeleccionada != null) {
            tableTramitesDiponibles.getStyleClass().add("drag-over");
        }
        event.consume();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Configura onDragOver y onDragExited en una tabla.
     * onDragOver acepta TransferMode.MOVE si el Dragboard tiene el formato correcto.
     * onDragExited limpia el highlight de drag-over.
     *
     * Se llama en initialize() para no duplicar lógica en las dos tablas.
     */
    private void configurarDragOver(TableView<?> tabla) {
        tabla.setOnDragOver(event -> {
            if (event.getDragboard().hasContent(TRAMITE_FORMAT)
                    && event.getGestureSource() != tabla) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        tabla.setOnDragExited(event -> {
            tabla.getStyleClass().remove("drag-over");
            event.consume();
        });
    }

    /** Muestra un Alert modal con el tipo y mensaje indicados. */
    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    @Override
    public void initialize() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}