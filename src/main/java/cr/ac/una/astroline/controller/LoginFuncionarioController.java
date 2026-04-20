package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Funcionario;
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

/**
 * Controlador de la vista de login de funcionarios.
 * Delega la autenticación a FuncionarioService y persiste
 * la sesión activa en SessionManager.
 * La redirección respeta el modo de acceso con que arrancó la app
 * (admin / funcionario), no solo el rol del usuario autenticado.
 *
 * @author JekaCordero
 */
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
        // Hook de Controller — se mantiene por consistencia con el resto de controladores
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicialización JavaFX — agregar binding o configuración de campos aquí si se necesita
    }

    @FXML
    private void onBtnIngresarLoginFuncionario(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        // Validación de campos vacíos
        if (username.isEmpty() || password.isEmpty()) {
            // TODO: reemplazar con alerta visual
            System.out.println("Por favor ingrese usuario y contraseña.");
            return;
        }

        Funcionario funcionario = funcionarioService.login(username, password);

        if (funcionario != null) {
            // Persiste la sesión para que otros controladores sepan quién está activo
            SessionManager.getInstancia().setFuncionarioActivo(funcionario);

            String modo = SessionManager.getInstancia().getModoAcceso();

            if ("admin".equals(modo)) {
                // Solo admins pueden acceder al módulo de administración
                if (!funcionario.esAdmin()) {
                    // TODO: reemplazar con alerta visual
                    System.out.println("Acceso denegado: se requiere perfil administrador.");
                    return;
                }
                FlowController.getInstance().goMain("Admin");
            } else {
                // Modo funcionario — tanto admins como funcionarios normales pueden entrar
                FlowController.getInstance().goMain("VentanaFuncionario");
            }

            getStage().close();

        } else {
            // TODO: reemplazar con alerta visual
            System.out.println("Credenciales incorrectas.");
        }
    }
    @FXML
    private void onCerrar(ActionEvent event) {
        getStage().close();
    }
}