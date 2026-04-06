
package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.util.FlowController;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;

/**
 * FXML Controller class
 *
 * @author takka_sama
 */
public class VerClienteController extends Controller implements Initializable {

    
    @FXML
    private ImageView imgFotoPerfil;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @Override
    public void initialize() {
       
    }
   
        @FXML
    private void OnActionEditarClientes(ActionEvent event) {
        FlowController.getInstance().goView("RegistroClienteView");
    }
}
