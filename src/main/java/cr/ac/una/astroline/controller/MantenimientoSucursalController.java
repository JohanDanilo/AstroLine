package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Estacion;
import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.model.Tramite;
import cr.ac.una.astroline.service.SucursalService;
import cr.ac.una.astroline.service.TramiteService;
import cr.ac.una.astroline.util.Respuesta;
import io.github.palexdev.materialfx.controls.MFXButton;
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
import javafx.util.StringConverter;

public class MantenimientoSucursalController extends Controller implements Initializable {

    @FXML
    private Label lblSucursal;
    @FXML
    private MFXComboBox<Sucursal> cmbSucursal;
    @FXML
    private MFXTextField txtBarraBusquedaEstaciones;
    @FXML
    private MFXButton btnAgregarSucursal;
    @FXML
    private MFXButton btnEliminarSucursal;
    @FXML
    private MFXButton btnAgregar;

    @FXML
    private ListView<Estacion> listaEstaciones;

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
        TRAMITE_FORMAT = existing != null
                ? existing
                : new DataFormat("application/astroline-tramite");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarComboSucursales();
        configurarColumnasTablas();
        configurarListView();
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
    private void onActionBtnAgregarSucursal() {
        String nombre = solicitarTexto(
                "Nueva sucursal",
                "Registrar sucursal",
                "Nombre de la sucursal:"
        );

        if (nombre == null) {
            return;
        }
        if (existeNombreSucursal(nombre)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Ya existe una sucursal con ese nombre.");
            return;
        }

        Sucursal nuevaSucursal = new Sucursal(
                sucursalService.generarIdSucursal(),
                nombre
        );

        Respuesta respuesta = sucursalService.agregarSucursal(nuevaSucursal);
        if (!respuesta.getEstado()) {
            mostrarAlerta(Alert.AlertType.ERROR, respuesta.getMensaje());
            return;
        }

        sucursalActualId = nuevaSucursal.getId();
        txtBarraBusquedaEstaciones.clear();
        refrescarSucursales();
    }

    @FXML
    private void onActionBtnEliminarSucursal() {
        if (sucursalActual == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecciona una sucursal primero.");
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Se eliminara la sucursal \"" + sucursalActual.getNombre()
                + "\" con todas sus estaciones.\nEsta accion no se puede deshacer.",
                ButtonType.YES,
                ButtonType.NO
        );
        confirmacion.setTitle("Confirmar eliminacion");
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

            if (idEliminada.equals(sucursalActualId)) {
                sucursalActualId = null;
            }
            txtBarraBusquedaEstaciones.clear();
            refrescarSucursales();
        });
    }

    @FXML
    private void onActionBtnAgregar() {
        if (sucursalActual == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecciona o crea una sucursal primero.");
            return;
        }

        String nombre = solicitarTexto(
                "Nueva estacion",
                "Agregar estacion en " + sucursalActual.getNombre(),
                "Nombre de la estacion:"
        );

        if (nombre == null) {
            return;
        }
        if (existeNombreEstacion(nombre)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Ya existe una estacion con ese nombre en la sucursal.");
            return;
        }

        String estacionId = sucursalService.generarIdEstacion();
        Estacion nueva = new Estacion(estacionId, nombre, sucursalActual.getId(), false, true);

        Respuesta respuesta = sucursalService.agregarEstacion(sucursalActual.getId(), nueva);
        if (!respuesta.getEstado()) {
            mostrarAlerta(Alert.AlertType.ERROR, respuesta.getMensaje());
            return;
        }

        refrescarSucursales();
        seleccionarEstacionPorId(estacionId);
    }

    @FXML
    private void onMousePressedEstaciones(MouseEvent event) {
        if (listaEstaciones.getSelectionModel().getSelectedItem() == null) {
            limpiarTramites();
            estacionSeleccionada = null;
            actualizarEstadoControles();
        }
    }

    @FXML
    private void onKeyPressedEstaciones(KeyEvent event) {
        if (event.getCode() != KeyCode.DELETE) {
            return;
        }

        Estacion seleccionada = listaEstaciones.getSelectionModel().getSelectedItem();
        if (seleccionada == null || sucursalActual == null) {
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Desea eliminar la estacion \"" + seleccionada.getNombre()
                + "\"?\nEsta accion no se puede deshacer.",
                ButtonType.YES,
                ButtonType.NO
        );
        confirmacion.setTitle("Confirmar eliminacion");
        confirmacion.setHeaderText(null);

        confirmacion.showAndWait().ifPresent(tipo -> {
            if (tipo != ButtonType.YES) {
                return;
            }

            Respuesta respuesta = sucursalService.eliminarEstacion(
                    sucursalActual.getId(),
                    seleccionada.getId()
            );

            if (!respuesta.getEstado()) {
                mostrarAlerta(Alert.AlertType.ERROR, respuesta.getMensaje());
                return;
            }

            refrescarSucursales();
        });
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

        Dragboard dragboard = tableTramitesAsignados.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.put(TRAMITE_FORMAT, seleccionado.getId());
        dragboard.setContent(content);
        event.consume();
    }

    @FXML
    private void onDragDroppedAsignados(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        boolean exito = false;

        if (dragboard.hasContent(TRAMITE_FORMAT) && estacionSeleccionada != null) {
            String tramiteId = (String) dragboard.getContent(TRAMITE_FORMAT);
            if (!estacionSeleccionada.atiendeTramite(tramiteId)) {
                boolean agregado = estacionSeleccionada.agregarTramite(tramiteId);
                if (agregado) {
                    exito = persistirEstacionSeleccionada();
                }
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

        Dragboard dragboard = tableTramitesDiponibles.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.put(TRAMITE_FORMAT, seleccionado.getId());
        dragboard.setContent(content);
        event.consume();
    }

    @FXML
    private void onDragDroppedDisponibles(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        boolean exito = false;

        if (dragboard.hasContent(TRAMITE_FORMAT) && estacionSeleccionada != null) {
            String tramiteId = (String) dragboard.getContent(TRAMITE_FORMAT);
            if (estacionSeleccionada.atiendeTramite(tramiteId)) {
                boolean quitado = estacionSeleccionada.quitarTramite(tramiteId);
                if (quitado) {
                    exito = persistirEstacionSeleccionada();
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

    @FXML
    private void onDragEntered(DragEvent event) {
        if (event.getDragboard().hasContent(TRAMITE_FORMAT) && estacionSeleccionada != null) {
            if (!tableTramitesDiponibles.getStyleClass().contains("drag-over")) {
                tableTramitesDiponibles.getStyleClass().add("drag-over");
            }
        }
        event.consume();
    }

    private void configurarComboSucursales() {
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
    }

    private void configurarColumnasTablas() {
        colTramiteAsignado.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getNombre()));
        colEstadoAsignado.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().isActivo() ? "Activo" : "Inactivo"));

        colTramiteDisponible.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getNombre()));
        colEstadoDisponible.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().isActivo() ? "Activo" : "Inactivo"));

        tableTramitesAsignados.setItems(tramitesAsignados);
        tableTramitesDiponibles.setItems(tramitesDisponibles);
    }

    private void configurarListView() {
        estacionesFiltradas = new FilteredList<>(estaciones, estacion -> true);
        listaEstaciones.setItems(estacionesFiltradas);

        listaEstaciones.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Estacion estacion, boolean empty) {
                super.updateItem(estacion, empty);
                if (empty || estacion == null) {
                    setText(null);
                    return;
                }

                String sufijoPreferencial = estacion.isPreferencial() ? "  *" : "";
                String sufijoEstado = estacion.isEstaActiva() ? "" : "  [Inactiva]";
                setText(estacion.getNombre() + sufijoPreferencial + sufijoEstado);
            }
        });

        listaEstaciones.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, nueva) -> cargarTramitesDeEstacion(nueva)
        );
    }

    private void configurarFiltro() {
        txtBarraBusquedaEstaciones.textProperty().addListener((obs, anterior, nuevo) -> {
            String criterio = nuevo == null ? "" : nuevo.trim().toLowerCase();
            estacionesFiltradas.setPredicate(estacion -> {
                if (criterio.isBlank()) {
                    return true;
                }
                return estacion.getNombre() != null
                        && estacion.getNombre().toLowerCase().contains(criterio);
            });
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
                ? estacionSeleccionada.getId()
                : null;

        estaciones.setAll(sucursalActual.getEstaciones());
        actualizarEncabezadoSucursal();

        if (estacionIdSeleccionada != null && seleccionarEstacionPorId(estacionIdSeleccionada)) {
            actualizarEstadoControles();
            return;
        }

        if (!estaciones.isEmpty()) {
            listaEstaciones.getSelectionModel().select(estaciones.get(0));
            return;
        }

        listaEstaciones.getSelectionModel().clearSelection();
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

        for (Tramite tramite : tramiteService.getListaDeTramites()) {
            if (estacion.atiendeTramite(tramite.getId())) {
                tramitesAsignados.add(tramite);
            } else {
                tramitesDisponibles.add(tramite);
            }
        }

        actualizarEstadoControles();
    }

    private boolean persistirEstacionSeleccionada() {
        if (sucursalActual == null || estacionSeleccionada == null) {
            return false;
        }

        String estacionId = estacionSeleccionada.getId();
        Respuesta respuesta = sucursalService.actualizarEstacion(
                sucursalActual.getId(),
                estacionSeleccionada
        );

        if (!respuesta.getEstado()) {
            mostrarAlerta(Alert.AlertType.ERROR, respuesta.getMensaje());
            return false;
        }

        refrescarSucursales();
        seleccionarEstacionPorId(estacionId);
        return true;
    }

    private void limpiarVistaSinSucursal() {
        if (cmbSucursal.getValue() != null) {
            cmbSucursal.setValue(null);
        }
        lblSucursal.setText("No hay sucursales registradas.");
        listaEstaciones.getSelectionModel().clearSelection();
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
        listaEstaciones.setDisable(!haySucursal);
        btnAgregar.setDisable(!haySucursal);
        btnEliminarSucursal.setDisable(!haySucursal);
        tableTramitesAsignados.setDisable(!hayEstacion);
        tableTramitesDiponibles.setDisable(!hayEstacion);
    }

    private void actualizarEncabezadoSucursal() {
        if (sucursalActual == null) {
            lblSucursal.setText("No hay sucursales registradas.");
            return;
        }

        int cantidadEstaciones = sucursalActual.getEstaciones().size();
        String sufijo = cantidadEstaciones == 1 ? "estacion" : "estaciones";
        lblSucursal.setText("Sucursal: " + sucursalActual.getNombre()
                + "  |  " + cantidadEstaciones + " " + sufijo);
    }

    private Sucursal buscarSucursalPorId(String sucursalId) {
        return sucursalId == null ? null : sucursalService.buscarSucursal(sucursalId);
    }

    private boolean seleccionarEstacionPorId(String estacionId) {
        if (estacionId == null) {
            return false;
        }

        for (Estacion estacion : estaciones) {
            if (estacionId.equals(estacion.getId())) {
                listaEstaciones.getSelectionModel().select(estacion);
                listaEstaciones.scrollTo(estacion);
                return true;
            }
        }

        return false;
    }

    private boolean existeNombreSucursal(String nombre) {
        for (Sucursal sucursal : sucursalService.getListaDeSucursales()) {
            if (sucursal.getNombre() != null && sucursal.getNombre().equalsIgnoreCase(nombre)) {
                return true;
            }
        }
        return false;
    }

    private boolean existeNombreEstacion(String nombre) {
        if (sucursalActual == null) {
            return false;
        }

        for (Estacion estacion : sucursalActual.getEstaciones()) {
            if (estacion.getNombre() != null && estacion.getNombre().equalsIgnoreCase(nombre)) {
                return true;
            }
        }
        return false;
    }

    private String solicitarTexto(String titulo, String encabezado, String etiqueta) {
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle(titulo);
        dialogo.setHeaderText(encabezado);
        dialogo.setContentText(etiqueta);

        return dialogo.showAndWait()
                .map(String::trim)
                .filter(texto -> !texto.isBlank())
                .orElse(null);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
