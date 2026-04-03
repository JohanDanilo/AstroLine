package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.util.FlowController;
import io.github.palexdev.materialfx.controls.MFXButton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

/**
 *
 * @author JekaCordero
 */
public class LoginFuncionarioController extends Controller implements Initializable {
    
    @FXML
    private MFXButton btnIngresarLoginFuncionario;
    
    @Override
    public void initialize() {
        
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }

    @FXML
    private void onBtnIngresarLoginFuncionario(ActionEvent event) {
        FlowController.getInstance().goMain("Funcionario");
    }

}
