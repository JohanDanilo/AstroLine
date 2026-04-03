package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.util.AppContext;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.GsonUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.util.List;

/**
 * Controller de la pantalla principal de selección de rol.
 * Carga la información de la empresa y navega al módulo seleccionado.
 *
 * @author AstroLine
 */
public class PrincipalController extends Controller {

    @FXML
    private ImageView imgLogo;

    @FXML
    private Label lblNombreEmpresa;

    @Override
    public void initialize() {
        cargarDatosEmpresa();
    }

    /**
     * Carga el logo y nombre de la empresa desde empresa.json.
     * Si el archivo no existe muestra los valores por defecto.
     */
    private void cargarDatosEmpresa() {
        
        Empresa empresa = GsonUtil.leer("empresa.json", Empresa.class);
        if (empresa == null) return;
        
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
        FlowController.getInstance().goMain("Admin"); 
    }

    @FXML
    private void irFuncionario() {
        FlowController.getInstance().goMain("LoginFuncionario");
    }

    @FXML
    private void irKiosko() {
        FlowController.getInstance().goMain("Kiosko");
    }

    @FXML
    private void irProyeccion() {
        FlowController.getInstance().goMain("Proyeccion");
    }
}