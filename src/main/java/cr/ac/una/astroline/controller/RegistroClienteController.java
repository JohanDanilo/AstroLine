package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Cliente;
import cr.ac.una.astroline.model.ClienteDTO;
import cr.ac.una.astroline.service.ClienteService;
import cr.ac.una.astroline.util.FlowController;

import io.github.palexdev.materialfx.controls.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;

public class RegistroClienteController extends Controller implements Initializable {

    @FXML private MFXDatePicker dpFechaNacimiento;
    @FXML private MFXTextField txtCedula;
    @FXML private MFXTextField txtNombre;
    @FXML private MFXTextField txtApellido;
    @FXML private MFXTextField txtTelefono;
    @FXML private MFXTextField txtCorreo;

    private final ClienteService clienteService = ClienteService.getInstancia();

    private Cliente editingCliente = null;

    // ─────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Aquí puedes inicializar cosas si ocupas
    }

    // ─────────────────────────────────────────────
    // CARGAR CLIENTE (modo edición)
    // ─────────────────────────────────────────────

    public void cargarClienteParaEditar(Cliente cliente) {
        if (cliente == null) return;

        editingCliente = cliente;

        ClienteDTO dto = new ClienteDTO();
        clienteService.cargarEnDTO(cliente, dto);

        txtCedula.setText(dto.getCedula());
        txtNombre.setText(dto.getNombre());
        txtApellido.setText(dto.getApellidos());
        txtTelefono.setText(dto.getTelefono());
        txtCorreo.setText(dto.getCorreo());
        dpFechaNacimiento.setValue(dto.getFechaNacimiento());

        txtCedula.setEditable(false);
    }

    // ─────────────────────────────────────────────
    // EVENTOS
    // ─────────────────────────────────────────────

    @FXML
    private void OnActionRegresarRegistroCliente(ActionEvent event) {
        FlowController.getInstance().goView("VerClienteView");
    }

    @FXML
    private void OnActionGuardarCambiosClientes(ActionEvent event) {

        if (!camposValidos()) return;

        Cliente cliente = construirClienteDesdeFormulario();

        if (editingCliente != null) {
            actualizarCliente(cliente);
        } else {
            registrarCliente(cliente);
        }
    }

    // ─────────────────────────────────────────────
    // LÓGICA SEPARADA (🔥 mejora importante)
    // ─────────────────────────────────────────────

    private Cliente construirClienteDesdeFormulario() {
        ClienteDTO dto = new ClienteDTO();

        dto.setCedula(txtCedula.getText().trim());
        dto.setNombre(txtNombre.getText().trim());
        dto.setApellidos(txtApellido.getText().trim());
        dto.setTelefono(txtTelefono.getText().trim());
        dto.setCorreo(txtCorreo.getText().trim());
        dto.setFotoPath(""); // pendiente
        dto.setFechaNacimiento(dpFechaNacimiento.getValue());

        return clienteService.dtoACliente(dto);
    }

    private void registrarCliente(Cliente cliente) {
        boolean guardado = clienteService.agregar(cliente);

        if (guardado) {
            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Éxito", "Cliente registrado correctamente.");
            limpiarFormulario();
            navegarALista();
        } else {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Error", "Ya existe un cliente con esa cédula.");
        }
    }

    private void actualizarCliente(Cliente cliente) {
        boolean actualizado = clienteService.actualizar(cliente);

        if (actualizado) {
            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Éxito", "Modificación realizada correctamente.");
            limpiarFormulario();
            navegarALista();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "Error", "No se pudo modificar el cliente.");
        }
    }

    private void navegarALista() {
        FlowController.getInstance().goView("VerClienteView");
    }

    // ─────────────────────────────────────────────
    // VALIDACIONES
    // ─────────────────────────────────────────────

    private boolean camposValidos() {

        if (txtCedula.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "La cédula es obligatoria.");
            return false;
        }

        if (txtNombre.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "El nombre es obligatorio.");
            return false;
        }

        if (txtApellido.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "El apellido es obligatorio.");
            return false;
        }

        if (!esFormatoCorreoValido(txtCorreo.getText().trim())) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Correo inválido", "Formato de correo incorrecto.");
            return false;
        }

        return true;
    }

    private boolean esFormatoCorreoValido(String correo) {
        if (correo == null || correo.trim().isEmpty()) return true;
        return correo.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    // ─────────────────────────────────────────────
    // UTILIDADES
    // ─────────────────────────────────────────────

    private void limpiarFormulario() {
        txtCedula.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        dpFechaNacimiento.setValue(null);

        txtCedula.setEditable(true);
        editingCliente = null;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    @Override
    public void initialize() {}
}