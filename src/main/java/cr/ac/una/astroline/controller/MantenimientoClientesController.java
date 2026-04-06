package cr.ac.una.astroline.controller;

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
    private ListView<ClienteDTO> listaDeClientes;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }    

    @Override
    public void initialize() {
    }

    @FXML
    private void onActionBtnAgregar(ActionEvent event) {
    
        FlowController.getInstance().goViewInPane("RegistroClienteView", paneContenedor); 
        
    }
     
}