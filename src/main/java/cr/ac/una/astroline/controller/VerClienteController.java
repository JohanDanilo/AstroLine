package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Cliente;
import cr.ac.una.astroline.service.ClienteService;
import cr.ac.una.astroline.util.FlowController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;


public class VerClienteController extends Controller implements Initializable {    
    @FXML
    private MFXButton btnAgregarClientes;
    @FXML
    private MFXButton btnEliminarCliente;
    @FXML
    private MFXButton btnEditarCliente;
    
     @FXML private TableView<Cliente> TbMostrarClientes;
    @FXML private TableColumn<Cliente, String> idColumn;
    @FXML private TableColumn<Cliente, String> nombreColumn;
    @FXML private TableColumn<Cliente, String> apellidoColumn;
    @FXML private TableColumn<Cliente, String> telefonoColumn;
    @FXML private TableColumn<Cliente, String> correoColumn;
    @FXML private TableColumn<Cliente, String> fechaColumn;

    private final ClienteService clienteService = ClienteService.getInstancia();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        cargarTabla();
    }    

    @Override
    public void initialize() {
    }
    
     private void configurarColumnas() {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("cedula"));
            nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
            apellidoColumn.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
            telefonoColumn.setCellValueFactory(new PropertyValueFactory<>("telefono"));
            correoColumn.setCellValueFactory(new PropertyValueFactory<>("correo"));
            fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaNacimiento"));        
        }
   private void cargarTabla() {
        TbMostrarClientes.setItems(clienteService.getListaDeClientes());
    }
        @FXML
    private void OnAgregarClientes(ActionEvent event) {
        FlowController.getInstance().goView("RegistroClienteView");
    }
    @FXML
    private void OnEditarCliente(ActionEvent event) {
        Cliente clienteSeleccionado = TbMostrarClientes.getSelectionModel().getSelectedItem();
        FlowController.getInstance().goView("RegistroClienteView");
        RegistroClienteController controller = (RegistroClienteController)
        FlowController.getInstance().getController("RegistroClienteView");
        controller.cargarClienteParaEditar(clienteSeleccionado);
    }
    @FXML
    private void OnEliminarCliente(ActionEvent event) {
        Cliente clienteSeleccionado = TbMostrarClientes.getSelectionModel().getSelectedItem();
         clienteService.remover(clienteSeleccionado);
    }

}