package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Tramite;
import cr.ac.una.astroline.service.TramiteService;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.Respuesta;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

public class MantenimientoTramitesController extends Controller implements Initializable {

    @FXML
    private AnchorPane root;
    @FXML
    private MFXTextField txtBusqueda;
    @FXML
    private TableView<Tramite> tblTramites;
    @FXML
    private TableColumn<Tramite, String> colId;
    @FXML
    private TableColumn<Tramite, String> colNombre;
    @FXML
    private TableColumn<Tramite, String> colDescripcion;
    @FXML
    private TableColumn<Tramite, String> colEstado;
    @FXML
    private MFXTextField txtId;
    @FXML
    private MFXTextField txtNombre;
    @FXML
    private MFXTextField txtDescripcion;
    @FXML
    private MFXCheckbox chkActivo;
    @FXML
    private MFXButton btnNuevo;
    @FXML
    private MFXButton btnGuardar;
    @FXML
    private MFXButton btnEliminar;

    private final TramiteService tramiteService = TramiteService.getInstancia();
    private FilteredList<Tramite> tramitesFiltrados;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        configurarBusqueda();
        configurarSeleccion();
        txtId.setEditable(false);
    }

    @Override
    public void initialize() {
        refrescarTabla();
        prepararNuevoTramite();
    }

    @FXML
    private void onActionBtnNuevo(ActionEvent event) {
        prepararNuevoTramite();
    }

    @FXML
    private void onActionBtnGuardar(ActionEvent event) {
        if (!camposValidos()) {
            return;
        }

        Tramite tramite = new Tramite(txtId.getText().trim(), txtNombre.getText().trim(), txtDescripcion.getText().trim(), chkActivo.isSelected()
        );

        boolean existe = tramiteService.buscarPorId(tramite.getId()) != null;
        Respuesta respuesta = existe ? tramiteService.actualizar(tramite) : tramiteService.agregar(tramite);

        mostrarAviso(respuesta.getMensaje());
        if (respuesta.getEstado()) {
            refrescarTabla();
            seleccionarTramite((Tramite) respuesta.getResultado("tramite"));
        }
    }

    @FXML
    private void onActionBtnEliminar(ActionEvent event) {
        Tramite seleccionado = tblTramites.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAviso("Seleccioná un trámite para eliminar.");
            return;
        }

        Respuesta respuesta = tramiteService.eliminar(seleccionado.getId());
        mostrarAviso(respuesta.getMensaje());
        if (respuesta.getEstado()) {
            refrescarTabla();
            prepararNuevoTramite();
        }
    }

    private void configurarTabla() {
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colDescripcion.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescripcion()));
        colEstado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isActivo() ? "Activo" : "Inactivo"));
    }

    private void configurarBusqueda() {
        txtBusqueda.textProperty().addListener((obs, oldValue, newValue) -> aplicarFiltro());
    }

    private void configurarSeleccion() {
        tblTramites.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, nuevo) -> {
            if (nuevo != null) {
                cargarTramite(nuevo);
            }
        });
    }

    private void refrescarTabla() {
        tramitesFiltrados = new FilteredList<>(tramiteService.getListaDeTramites(), t -> true);
        tblTramites.setItems(tramitesFiltrados);
        aplicarFiltro();
        tblTramites.refresh();
    }

    private void aplicarFiltro() {
        if (tramitesFiltrados == null) {
            return;
        }

        String criterio = txtBusqueda.getText() == null ? "" : txtBusqueda.getText().trim().toLowerCase();

        tramitesFiltrados.setPredicate(tramite -> {
            if (criterio.isBlank()) {
                return true;
            }
            return tramite.getId().toLowerCase().contains(criterio) || tramite.getNombre().toLowerCase().contains(criterio) || tramite.getDescripcion().toLowerCase().contains(criterio);
        });
    }

    private void prepararNuevoTramite() {
        tblTramites.getSelectionModel().clearSelection();
        txtId.setText(tramiteService.generarSiguienteId());
        txtNombre.clear();
        txtDescripcion.clear();
        chkActivo.setSelected(true);
    }

    private void cargarTramite(Tramite tramite) {
        txtId.setText(tramite.getId());
        txtNombre.setText(tramite.getNombre());
        txtDescripcion.setText(tramite.getDescripcion());
        chkActivo.setSelected(tramite.isActivo());
    }

    private void seleccionarTramite(Tramite tramite) {
        if (tramite == null) {
            prepararNuevoTramite();
            return;
        }

        for (Tramite item : tblTramites.getItems()) {
            if (item.getId().equals(tramite.getId())) {
                tblTramites.getSelectionModel().select(item);
                tblTramites.scrollTo(item);
                cargarTramite(item);
                return;
            }
        }
        prepararNuevoTramite();
    }

    private boolean camposValidos() {
        if (txtId.getText() == null || txtId.getText().isBlank()) {
            mostrarAviso("El ID del trámite no puede estar vacío.");
            return false;
        }
        if (txtNombre.getText() == null || txtNombre.getText().isBlank()) {
            mostrarAviso("El nombre del trámite es obligatorio.");
            return false;
        }
        return true;
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
