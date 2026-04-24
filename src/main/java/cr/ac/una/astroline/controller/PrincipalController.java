package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.service.ConfiguracionService;
import cr.ac.una.astroline.util.AppContext;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.GsonUtil;
import cr.ac.una.astroline.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

public class PrincipalController extends Controller {

    @FXML
    private ImageView imgLogo;

    @FXML
    private Label lblNombreEmpresa;

    private static String acceso = "";

    @Override
    public void initialize() {
        cargarDatosEmpresa();
    }

    private void cargarDatosEmpresa() {

        Empresa empresa = GsonUtil.leer("empresa.json", Empresa.class);
        if (empresa == null) {
            return;
        }

        AppContext.getInstance().set("empresa", empresa);

        if (lblNombreEmpresa != null && empresa.getNombre() != null) {
            lblNombreEmpresa.setText(empresa.getNombre());
        }

        if (imgLogo != null && empresa.getLogoPath() != null) {
            File archivoLogo = new File(empresa.getLogoPath());
            if (archivoLogo.exists()) {
                imgLogo.setImage(new Image(archivoLogo.toURI().toString()));
            }
        }
    }

    @FXML
    private void irAdministrador() {
        SessionManager.getInstancia().setModoAcceso("admin");
        FlowController.getInstance().goViewInWindow("LoginFuncionarioView");
        getStage().close();
    }

    @FXML
    private void irFuncionario() {
        SessionManager.getInstancia().setModoAcceso("funcionario");
        FlowController.getInstance().goViewInWindow("LoginFuncionarioView");
        getStage().close();
    }

    @FXML
    private void irKiosko() {
        SessionManager.getInstancia().setModoAcceso("kiosko");
        if (!ConfiguracionService.getInstancia().estaConfiguradoParaModo("kiosko")) {
            FlowController.getInstance().goViewInWindow("SeleccionSucursalView");
        } else {
            FlowController.getInstance().goMain("Kiosko");
        }
        getStage().close();
    }

    @FXML
    private void irProyeccion() {
        SessionManager.getInstancia().setModoAcceso("proyeccion");
        if (!ConfiguracionService.getInstancia().estaConfiguradoParaModo("proyeccion")) {
            FlowController.getInstance().goViewInWindow("SeleccionSucursalView");
        } else {
            FlowController.getInstance().goMain("Proyeccion");
        }
        getStage().close();
    }
    
}
