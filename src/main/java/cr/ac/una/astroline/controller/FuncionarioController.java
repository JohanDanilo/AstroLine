package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Funcionario;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.SessionManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Controller placeholder del módulo Funcionario.
 * Será implementado por Jessica.
 *
 */
public class FuncionarioController extends Controller implements Initializable {

    @FXML
    private ImageView logoEmpresa;
    @FXML
    private Label nombreEmpresa;
    @FXML
    private Label usuarioFuncionario;
    @FXML
    private MFXButton BtnConfiguracionEstacion;
    @FXML
    private MFXButton BtnRegistroClientes;
    @FXML
    private MFXButton BtnAtencionDeClientes;
    @FXML
    private MFXButton bntLlamarSiguiente;
    @FXML
    private MFXButton btnLlamarPreferencial;
    @FXML
    private MFXButton btnRepetirLlamado;
    @FXML
    private MFXButton btnSeleccionarFicha;
    @FXML
    private Label nombreEstación;
    @FXML
    private Label nombreSucursal;
    @FXML
    private MFXButton btnCerrarSesión;
    @FXML
    private VBox contenedor;

    @Override
    public void initialize() {
        setNombreVista("Funcionario");
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
            Funcionario funcionario = SessionManager.getInstancia().getFuncionarioActivo();
        if (funcionario != null) {
            usuarioFuncionario.setText(funcionario.getUsername()); // getNombreCompleto());
        }
    }    
    @FXML
    private void onBtnFicha(ActionEvent event) {
        FlowController.getInstance().goView("FichaFuncionarioView");
    }

    @FXML
    private void onBtnRegistroClientes(ActionEvent event) {
        FlowController.getInstance().goView("VerClienteView");
    }
    
    @FXML
    private void onBtnSeleccionarFicha(ActionEvent event) {
        FlowController.getInstance().goViewInWindow("FuncionarioSeleccionarFichaView");
    }
    
    @FXML
    private void onBtnCerrarSesion(ActionEvent event) {
        FlowController.getInstance().goViewInWindow("LoginFuncionarioView");
        getStage().close();
    }
    @FXML
    private void onBtnConfiguracionEstacion(ActionEvent event) {
        FlowController.getInstance().goView("ConfiguracionView");
    }
}