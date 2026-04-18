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
import javafx.scene.layout.AnchorPane;

/**
 * Controller del formulario de registro/edición de estaciones.
 * Se abre como ventana modal desde MantenimientoSucursalController.
 *
 * Datos de entrada via AppContext (deben setearse ANTES de abrir la ventana):
 *   "sucursalParaEstacion" → String  (obligatorio — ID de la sucursal destino)
 *   "estacionParaEditar"   → Estacion (opcional — si es null, modo CREAR)
 *
 * @author JohanDanilo
 */
public class RegistroEstacionController extends Controller implements Initializable {

    @FXML private AnchorPane root;
    @FXML private MFXTextField txtNombre;
    @FXML private MFXCheckbox  checkActivo;
    @FXML private MFXCheckbox  chekPreferencial;   // nombre del FXML respetado (typo original)
    @FXML private MFXButton    btnAgregar;
    @FXML private MFXButton    btnCancelar;

    private final SucursalService sucursalService = SucursalService.getInstancia();

    /** ID de la sucursal donde se agrega/edita la estación. */
    private String sucursalId;

    /** Estación a editar. Null en modo creación. */
    private Estacion estacionParaEditar;

    /** true si la ventana fue abierta para editar una estación existente. */
    private boolean esEdicion;

    // ── Inicialización ────────────────────────────────────────────────────────

    @Override
    public void initialize() {
        // Hook de Controller — datos se leen en initialize(URL, ResourceBundle)
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Leer datos pasados por MantenimientoSucursalController via AppContext
        sucursalId = (String) AppContext.getInstance().get("sucursalParaEstacion");
        estacionParaEditar = (Estacion) AppContext.getInstance().get("estacionParaEditar");
        esEdicion = estacionParaEditar != null;

        if (esEdicion) {
            // Modo edición: precarga los datos y cambia la etiqueta del botón
            txtNombre.setText(estacionParaEditar.getNombre());
            checkActivo.setSelected(estacionParaEditar.isEstaActiva());
            chekPreferencial.setSelected(estacionParaEditar.isPreferencial());
            btnAgregar.setText("Guardar");
            // No se puede cambiar el ID — es la clave de la estación
            txtNombre.requestFocus();
        } else {
            // Modo creación: valores por defecto
            checkActivo.setSelected(true);
            chekPreferencial.setSelected(false);
            txtNombre.requestFocus();
        }

        // Limpiar context para no contaminar proximas aperturas
        AppContext.getInstance().delete("estacionParaEditar");
    }

    // ── Eventos ───────────────────────────────────────────────────────────────

    @FXML
    private void onActionBttnAgregar(ActionEvent event) {
        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "El nombre de la estación es obligatorio.");
            return;
        }

        if (sucursalId == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error interno", "No se recibió la sucursal destino.");
            return;
        }

        if (esEdicion) {
            guardarEdicion(nombre);
        } else {
            guardarNuevaEstacion(nombre);
        }
    }

    @FXML
    private void onActionBtnCancelar(ActionEvent event) {
        getStage().close();
    }

    // ── Lógica de guardado ────────────────────────────────────────────────────

    private void guardarNuevaEstacion(String nombre) {
        // Verificar que no haya otra estación con el mismo nombre en la misma sucursal
        if (existeNombreEnSucursal(nombre, null)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Nombre duplicado",
                    "Ya existe una estación con ese nombre en la sucursal.");
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
            // Dejar el ID en AppContext para que MantenimientoSucursalController
            // pueda seleccionar la nueva estación automáticamente
            AppContext.getInstance().set("ultimaEstacionAgregadaId", estacionId);
            getStage().close();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al agregar", respuesta.getMensaje());
        }
    }

    private void guardarEdicion(String nombre) {
        // Verificar nombre duplicado excluyendo la propia estación editada
        if (existeNombreEnSucursal(nombre, estacionParaEditar.getId())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Nombre duplicado",
                    "Ya existe otra estación con ese nombre en la sucursal.");
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

    // ── Utilidades ────────────────────────────────────────────────────────────

    /**
     * Verifica si ya existe una estación con el mismo nombre en la sucursal.
     * Si excluirId no es null, ignora esa estación (modo edición).
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
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}