package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Cliente;
import cr.ac.una.astroline.service.ClienteService;
import cr.ac.una.astroline.util.FlowController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


import cr.ac.una.astroline.model.ClienteDTO;
import javafx.scene.control.Alert;
/*
*
*
*       encargada de la vista de registro del cliente
*
*
*/
public class RegistroClienteController extends Controller implements Initializable {
   
    private AnchorPane root;
    @FXML
    private MFXButton btnSubirFoto;
    @FXML
    private MFXButton btnAbrirCamara;
    @FXML
    private MFXButton btnTomarFoto;
    @FXML
    private MFXDatePicker dpFechaNacimiento;
    @FXML
    private MFXTextField txtCedula;
    @FXML
    private MFXTextField txtNombre;
    @FXML
    private MFXTextField txtApellido;
    @FXML
    private MFXTextField txtTelefono;
    @FXML
    private MFXTextField txtCorreo;
    @FXML
    private MFXButton btnGuardarCambiosClientes;
    @FXML
    private MFXButton btnRegresarAListaClientes;
    
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final ClienteService clienteService = ClienteService.getInstancia();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        }    

    @Override
    public void initialize() {
      
    }
    private Cliente EditandoCliente = null;

    public void cargarClienteParaEditar(Cliente cliente) {
        if (cliente == null) return;

        EditandoCliente = cliente;

        // Cargar los datos 
        ClienteDTO dto = new ClienteDTO();
        clienteService.cargarEnDTO(cliente, dto);

        txtCedula.setText(dto.getCedula());
        txtNombre.setText(dto.getNombre());
        txtApellido.setText(dto.getApellidos());
        txtTelefono.setText(dto.getTelefono());
        txtCorreo.setText(dto.getCorreo());
        dpFechaNacimiento.setValue(dto.getFechaNacimiento());
        txtCedula.setEditable(false); //La cedula no se puede cambiar
    }
    
    @FXML
    private void OnActionRegresarRegistroCliente(ActionEvent event) {
        FlowController.getInstance().goView("VerClienteView");
    }

    @FXML
    private void OnActionGuardarCambiosClientes(ActionEvent event) {
        if (!camposValidos()) return;
        ClienteDTO dto = new ClienteDTO();
        dto.setCedula(txtCedula.getText().trim());
        dto.setNombre(txtNombre.getText().trim());
        dto.setApellidos(txtApellido.getText().trim());
        dto.setTelefono(txtTelefono.getText().trim());
        dto.setCorreo(txtCorreo.getText().trim());
        dto.setFotoPath("");  // Sin foto
        LocalDate fecha = dpFechaNacimiento.getValue();
        dto.setFechaNacimiento(fecha);  

        Cliente cliente = clienteService.dtoACliente(dto);

    if (EditandoCliente != null) {
        // Editando
        boolean actualizado = clienteService.actualizar(cliente);
        if (actualizado) {
            mostrarAlerta(Alert.AlertType.INFORMATION,
                "Éxito", "Modificación realizada correctamente.");
            limpiarFormulario();
            FlowController.getInstance().goView("VerClienteView");    
        } else {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "Error", "No se pudo modificar el cliente.");
        }
    } else {
        // Registrando
        boolean guardado = clienteService.agregar(cliente);
        if (guardado) {
            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Éxito", "Cliente registrado correctamente.");
            limpiarFormulario();
            FlowController.getInstance().goView("VerClienteView");
        } else {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Error", "Ya existe un cliente con esa cédula.");
        }
    }
 }
    
    
    private boolean esFormatoCorreoValido(String correo) {
        if (correo == null || correo.trim().isEmpty()) return true; 
        return correo.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }
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
        mostrarAlerta(Alert.AlertType.WARNING, "Correo inválido",
                "El correo debe tener el formato de correo electrónico");
        return false;
    }
        return true;
    }

    private void limpiarFormulario() {
        txtCedula.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        dpFechaNacimiento.setValue(null);
        txtCedula.setEditable(true); 
        EditandoCliente = null; 
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
   
}
