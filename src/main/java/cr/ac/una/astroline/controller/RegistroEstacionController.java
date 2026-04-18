package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Estacion;
import cr.ac.una.astroline.service.SucursalService;
import cr.ac.una.astroline.util.AppContext;
import cr.ac.una.astroline.util.Respuesta;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;

/**
 * Controlador del formulario de registro/edicion de estaciones.
 * Se abre como modal desde MantenimientoSucursalController.
 *
 * Datos de entrada via AppContext (seteados ANTES de abrir la ventana):
 *   "sucursalParaEstacion" -> String   (obligatorio — ID de la sucursal destino)
 *   "estacionParaEditar"   -> Estacion (opcional — null = CREAR, non-null = EDITAR)
 *
 * Al cerrar correctamente deposita en AppContext:
 *   "ultimaEstacionAgregadaId" -> String (ID de la estacion creada o editada)
 *
 * IMPORTANTE — Ciclo de vida:
 *   initialize(URL, ResourceBundle) -> solo configura componentes estaticos.
 *   initialize()                    -> hook de FlowController; aqui se lee
 *                                      AppContext porque en este punto el stage
 *                                      y el contexto estan listos.
 *
 * @author JohanDanilo
 */
public class RegistroEstacionController extends Controller implements Initializable {

    @FXML private AnchorPane  root;
    @FXML private MFXTextField txtNombre;
    @FXML private MFXCheckbox  checkActivo;
    @FXML private MFXCheckbox  chekPreferencial;   // typo del FXML respetado
    @FXML private MFXButton    btnAgregar;
    @FXML private MFXButton    btnCancelar;

    private final SucursalService sucursalService = SucursalService.getInstancia();

    private String   sucursalId;
    private Estacion estacionParaEditar;
    private boolean  esEdicion;

    /**
     * Llamado por FXMLLoader durante load().
     * Solo configuracion estatica — NO leer AppContext aqui.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Sin logica de datos — ver initialize() abajo
    }

    /**
     * Hook de FlowController. Corre DESPUES de que el stage esta configurado
     * y AppContext fue seteado por el padre. Aqui se detecta el modo y
     * se precargan los campos.
     */
    @Override
    public void initialize() {
        sucursalId         = (String)   AppContext.getInstance().get("sucursalParaEstacion");
        estacionParaEditar = (Estacion) AppContext.getInstance().get("estacionParaEditar");
        AppContext.getInstance().delete("estacionParaEditar");  // limpiar para proxima apertura

        esEdicion = estacionParaEditar != null;

        // Valores por defecto para modo creacion
        checkActivo.setSelected(true);
        chekPreferencial.setSelected(false);

        if (esEdicion) {
            // Precargar datos de la estacion existente
            txtNombre.setText(estacionParaEditar.getNombre());
            checkActivo.setSelected(estacionParaEditar.isEstaActiva());
            chekPreferencial.setSelected(estacionParaEditar.isPreferencial());
            btnAgregar.setText("Guardar");
        } else {
            btnAgregar.setText("Agregar");
        }

        txtNombre.requestFocus();
    }

    @FXML
    private void onActionBttnAgregar(ActionEvent event) {
        String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();

        if (nombre.isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Campo requerido", "El nombre de la estacion es obligatorio.");
            return;
        }

        if (sucursalId == null) {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "Error interno", "No se recibio la sucursal destino.");
            return;
        }

        if (esEdicion) {
            guardarEdicion(nombre);
        } else {
            guardarNueva(nombre);
        }
    }

    @FXML
    private void onActionBtnCancelar(ActionEvent event) {
        getStage().close();
    }

    private void guardarNueva(String nombre) {
        if (existeNombreEnSucursal(nombre, null)) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Nombre duplicado", "Ya existe una estacion con ese nombre en la sucursal.");
            return;
        }

        String estacionId = sucursalService.generarIdEstacion();
        Estacion nueva = new Estacion(
                estacionId,
                nombre,
                sucursalId,
                chekPreferencial.isSelected(),
                checkActivo.isSelected()
        );

        Respuesta respuesta = sucursalService.agregarEstacion(sucursalId, nueva);

        if (respuesta.getEstado()) {
            AppContext.getInstance().set("ultimaEstacionAgregadaId", estacionId);
            getStage().close();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al agregar", respuesta.getMensaje());
        }
    }

    private void guardarEdicion(String nombre) {
        if (existeNombreEnSucursal(nombre, estacionParaEditar.getId())) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Nombre duplicado", "Ya existe otra estacion con ese nombre en la sucursal.");
            return;
        }

        estacionParaEditar.setNombre(nombre);
        estacionParaEditar.setPreferencial(chekPreferencial.isSelected());
        estacionParaEditar.setEstaActiva(checkActivo.isSelected());

        Respuesta respuesta = sucursalService.actualizarEstacion(sucursalId, estacionParaEditar);

        if (respuesta.getEstado()) {
            AppContext.getInstance().set("ultimaEstacionAgregadaId", estacionParaEditar.getId());
            getStage().close();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al guardar", respuesta.getMensaje());
        }
    }

    /**
     * Verifica si ya existe una estacion con el mismo nombre en la sucursal.
     * Si excluirId no es null, ignora esa estacion (modo edicion).
     */
    private boolean existeNombreEnSucursal(String nombre, String excluirId) {
        var sucursal = sucursalService.buscarSucursal(sucursalId);
        if (sucursal == null) return false;

        for (Estacion e : sucursal.getEstaciones()) {
            if (excluirId != null && excluirId.equals(e.getId())) continue;
            if (nombre.equalsIgnoreCase(e.getNombre())) return true;
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