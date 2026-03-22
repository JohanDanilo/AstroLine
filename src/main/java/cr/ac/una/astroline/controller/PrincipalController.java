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

    @FXML
    private Label lblVersion;

    @Override
    public void initialize() {
        setNombreVista("AstroLine — Selección de acceso");
        cargarDatosEmpresa();
    }

    /**
     * Carga el logo y nombre de la empresa desde empresa.json.
     * Si el archivo no existe muestra los valores por defecto.
     */
    private void cargarDatosEmpresa() {
        Empresa empresa = GsonUtil.leer("empresa.json", Empresa.class);

        if (empresa != null) {
            // Guardar empresa en contexto global para que todos los módulos la usen
            AppContext.getInstance().set("empresa", empresa);

            // Mostrar nombre
            if (empresa.getNombre() != null && !empresa.getNombre().isEmpty()) {
                lblNombreEmpresa.setText(empresa.getNombre());
            }

            // Mostrar logo
            if (empresa.getLogoPath() != null && !empresa.getLogoPath().isEmpty()) {
                File archivoLogo = new File(empresa.getLogoPath());
                if (archivoLogo.exists()) {
                    imgLogo.setImage(new Image(archivoLogo.toURI().toString()));
                }
            }
        }
    }

    @FXML
    private void irAdministrador() {
        FlowController.getInstance().goViewInWindow("AdminView");
    }

    @FXML
    private void irFuncionario() {
        FlowController.getInstance().goViewInWindow("FuncionarioView");
    }

    @FXML
    private void irKiosko() {
        FlowController.getInstance().goViewInWindow("KioskoView");
    }

    @FXML
    private void irProyeccion() {
        FlowController.getInstance().goViewInWindow("ProyeccionView");
    }
}