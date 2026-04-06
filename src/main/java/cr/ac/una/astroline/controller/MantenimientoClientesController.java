package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Cliente;
import cr.ac.una.astroline.model.ClienteDTO;
import cr.ac.una.astroline.util.FlowController;
import io.github.palexdev.materialfx.controls.MFXButton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import cr.ac.una.astroline.model.MantenimientoDeClientes;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
/*
*
*
*       encargada de la construir la vista general de clientes
*
*
*/

public class MantenimientoClientesController extends Controller implements Initializable {

    @FXML
    private AnchorPane root;
    @FXML
    private MFXButton btnAgregar;
    @FXML
    private VBox paneContenedor;    
    @FXML
    private MFXTextField txxBuscarCliente;
    @FXML
    private TableView<Cliente> tablaClientes;
    
    MantenimientoDeClientes clientes;  
    @FXML
    private TableColumn<Cliente, String> colName;
    @FXML
    private TableColumn<Cliente, String> colCedula;
    
    public void initialize(URL url, ResourceBundle rb) {
        clientes = MantenimientoDeClientes.getInstancia();
        
        colName.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCedula.setCellValueFactory(new PropertyValueFactory<>("cedula"));
        
        tablaClientes.setItems(clientes.getListaDeClientes());     
    }    

    @Override
    public void initialize() {
    }

    @FXML
    private void onActionBtnAgregar(ActionEvent event) {
        
        FlowController.getInstance().goViewInDividePane("RegistroClienteView", paneContenedor, true); 
        
    }
   
    
}
