package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.util.FlowController;
import io.github.palexdev.materialfx.controls.MFXButton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

/**
 * Controller placeholder del módulo Funcionario.
 * Será implementado por Jessica.
 *
 */
public class FuncionarioController extends Controller implements Initializable {

    @FXML
    private MFXButton btnRegistro;

    @Override
    public void initialize() {
        setNombreVista("Funcionario");
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    @FXML
    private void onBtnFicha(ActionEvent event) {
        FlowController.getInstance().goView("FichaFuncionarioView");
    }

    @FXML
    private void onBtnRegistro(ActionEvent event) {
        FlowController.getInstance().goView("RegistroFuncionarioView");
    }
    
    @FXML
    private void onBtnSeleccionarFicha(ActionEvent event) {
        FlowController.getInstance().goViewInWindow("FuncionarioSeleccionarFichaView");
    }
    
    @FXML
    private void onBtnCerrarSesion(ActionEvent event) {
        FlowController.getInstance().goMain("LoginFuncionario");
    }
}