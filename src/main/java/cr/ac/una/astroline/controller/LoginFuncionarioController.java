package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.ConfiguracionLocal;
import cr.ac.una.astroline.model.Funcionario;
import cr.ac.una.astroline.service.ConfiguracionService;
import cr.ac.una.astroline.service.FuncionarioService;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.SessionManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

public class LoginFuncionarioController extends Controller implements Initializable {

    @FXML
    private MFXButton btnIngresarLoginFuncionario;
    @FXML
    private MFXTextField txtUsername;
    @FXML
    private MFXPasswordField txtPassword;

    private final FuncionarioService funcionarioService = FuncionarioService.getInstancia();

    @Override
    public void initialize() {
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    private void onBtnIngresarLoginFuncionario(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            mostrarAviso("Por favor ingrese usuario y contraseña.");
            return;
        }

        Funcionario funcionario = funcionarioService.login(username, password);

        if (funcionario == null) {
            mostrarAviso("Credenciales incorrectas. Verifique su usuario y contraseña.");
            return;
        }

        SessionManager.getInstancia().setFuncionarioActivo(funcionario);
        String modo = SessionManager.getInstancia().getModoAcceso();

        if (!"admin".equals(modo) && !hayConfiguracionValida()) {
            mostrarAviso(
                "Este equipo no tiene una estación configurada.\n"
                + "Debe configurarlo mediante el modulo admin\n"
                + " o en la ruta: data/configuracion.json"
            );
            return;
        }

        if ("admin".equals(modo)) {
            if (!funcionario.esAdmin()) {
                mostrarAviso("Acceso denegado: se requiere perfil administrador.");
                return;
            }
            FlowController.getInstance().goMain("Admin");
        } else {
            FlowController.getInstance().goMain("VentanaFuncionario");
        }

        getStage().close();
    }

    @FXML
    private void onCerrar(ActionEvent event) {
        getStage().close();
    }
    
    private boolean hayConfiguracionValida() {
        ConfiguracionLocal config = ConfiguracionService.getInstancia().getConfiguracion();
        return config != null
                && config.getSucursalId() != null
                && !config.getSucursalId().isBlank();
    }

    private void mostrarAviso(String mensaje) {
        Controller controller = FlowController.getInstance().getController("AvisoView");
        if (controller instanceof AvisoController avisoController) {
            avisoController.cambiarInformacionDeAviso(mensaje);
        }
        FlowController.getInstance().goViewInWindowModal(
                "AvisoView",
                getStage(),
                false
        );
    }
}