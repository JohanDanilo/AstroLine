package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.service.SucursalService;
import cr.ac.una.astroline.util.AppContext;
import cr.ac.una.astroline.util.Respuesta;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

public class RegistroSucursalController extends Controller implements Initializable {

    @FXML
    private AnchorPane root;
    @FXML
    private Label lblTitulo;
    @FXML
    private MFXTextField txtNombre;
    @FXML
    private TextArea txtAreaAviso;
    @FXML
    private MFXButton btnGuardar;
    @FXML
    private MFXButton btnCancelar;

    private final SucursalService sucursalService = SucursalService.getInstancia();

    private Sucursal sucursalParaEditar;
    private boolean esEdicion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    @Override
    public void initialize() {
        sucursalParaEditar = (Sucursal) AppContext.getInstance().get("sucursalParaEditar");
        AppContext.getInstance().delete("sucursalParaEditar");

        esEdicion = sucursalParaEditar != null;

        if (esEdicion) {
            lblTitulo.setText("Editar Sucursal");
            btnGuardar.setText("Guardar cambios");
            txtNombre.setText(sucursalParaEditar.getNombre());
            txtAreaAviso.setText(sucursalParaEditar.getTextoAviso());
        } else {
            lblTitulo.setText("Nueva Sucursal");
            btnGuardar.setText("Agregar");
        }

        txtNombre.requestFocus();
    }

    @FXML
    private void onActionBtnGuardar(ActionEvent event) {
        String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
        String aviso = txtAreaAviso.getText() == null ? "" : txtAreaAviso.getText().trim();

        if (nombre.isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Campo requerido", "El nombre de la sucursal es obligatorio.");
            return;
        }

        if (esEdicion) {
            guardarEdicion(nombre, aviso);
        } else {
            guardarNueva(nombre, aviso);
        }
    }

    @FXML
    private void onActionBtnCancelar(ActionEvent event) {
        getStage().close();
    }

    private void guardarNueva(String nombre, String aviso) {
        if (existeNombre(nombre, null)) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Nombre duplicado", "Ya existe una sucursal con ese nombre.");
            return;
        }

        String id = sucursalService.generarIdSucursal();
        Sucursal nueva = new Sucursal(id, nombre);
        nueva.setTextoAviso(aviso);

        Respuesta respuesta = sucursalService.agregarSucursal(nueva);

        if (respuesta.getEstado()) {
            AppContext.getInstance().set("ultimaSucursalId", id);
            getStage().close();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al agregar", respuesta.getMensaje());
        }
    }

    private void guardarEdicion(String nombre, String aviso) {
        if (existeNombre(nombre, sucursalParaEditar.getId())) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Nombre duplicado", "Ya existe otra sucursal con ese nombre.");
            return;
        }

        sucursalParaEditar.setNombre(nombre);
        sucursalParaEditar.setTextoAviso(aviso);

        Respuesta respuesta = sucursalService.actualizarSucursal(sucursalParaEditar);

        if (respuesta.getEstado()) {
            AppContext.getInstance().set("ultimaSucursalId", sucursalParaEditar.getId());
            getStage().close();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al guardar", respuesta.getMensaje());
        }
    }

    private boolean existeNombre(String nombre, String excluirId) {
        for (Sucursal s : sucursalService.getListaDeSucursales()) {
            if (excluirId != null && excluirId.equals(s.getId())) {
                continue;
            }
            if (nombre.equalsIgnoreCase(s.getNombre())) {
                return true;
            }
        }
        return false;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
