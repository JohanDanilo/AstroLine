package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.model.Funcionario;
import cr.ac.una.astroline.service.EmpresaService;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.GsonUtil;
import cr.ac.una.astroline.util.SessionManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
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
    
    Empresa empresa = EmpresaService.getInstancia().getEmpresa();

    @Override
    public void initialize() {
        setNombreVista("Funcionario");
        cargarEmpresa();
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
    
    private void cargarEmpresa() {
        
        if (empresa == null) return;

        nombreEmpresa.setText(empresa.getNombre());

        if (empresa.getLogoPath() != null && !empresa.getLogoPath().isBlank()) {
            try {
                // Esto extrae SOLO el nombre del archivo (ejemplo: "logo_empresa.png")
                // sin importar qué carpetas traiga el String original
                String nombreSolo = new java.io.File(empresa.getLogoPath()).getName();

                // Ahora construimos la ruta limpia
                java.io.File archivoLogo = new java.io.File("data/logoEmpresa/" + nombreSolo);

                if (archivoLogo.exists()) {
                    logoEmpresa.setImage(new Image(archivoLogo.toURI().toString()));
                } else {
                    System.err.println("[FuncionarioController] No encontrado en: " + archivoLogo.getAbsolutePath());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        
        }
    }
}