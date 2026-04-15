package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.App;
import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.GsonUtil;
import io.github.palexdev.materialfx.controls.MFXButton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Controller placeholder del módulo Administrador.
 * Será implementado por José.
 *
 */
public class AdminController extends Controller implements Initializable {

    @FXML
    private ImageView logoEmpresa;
    @FXML
    private Label nombreEmpresa; 
    @FXML
    private MFXButton btnConfiguracionGeneral;
    
    @FXML
    private MFXButton btnTramites;
    @FXML
    private MFXButton btnRankings;
    @FXML
    private MFXButton btnClientes;  
    @FXML
    private MFXButton btnConfigEstacion;

    private Empresa empresa;
    @FXML
    private MFXButton btnSucursales;
    
    @Override
    public void initialize() {
        setNombreVista("Administrador");
        cargarEmpresa();
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    private void onActionBtnClientes(ActionEvent event) {
        FlowController.getInstance().goView("MantenimientoClientesView");
    }

    @FXML
    private void onActionBtnVerClientes(ActionEvent event) {
        FlowController.getInstance().goView("VerClienteView");
    }

    @FXML
    private void onActionBtnGeneral (ActionEvent event) {
        FlowController.getInstance().goView("MantenimientoParametrosGeneralesView");
    }

    @FXML
    private void onBtnConfigEstacion(ActionEvent event) {
        FlowController.getInstance().goView("MantenimientoEstacionesView");
    }

    @FXML
    private void onBtnTramites(ActionEvent event) {
        FlowController.getInstance().goView("MantenimientoTramitesView");
    }
    
    @FXML
    private void onBtnSucursales(ActionEvent event) {
        FlowController.getInstance().goView("MantenimientoSucursalView");
    }
    
    // -------------------------------------------------------------------------
    // CARGA INICIAL
    // -------------------------------------------------------------------------

    private void cargarEmpresa() {
        empresa = GsonUtil.leer("empresa.json", Empresa.class);
        if (empresa == null) return;

        nombreEmpresa.setText(empresa.getNombre());

        if (empresa.getLogoPath() != null && !empresa.getLogoPath().isBlank()) {
            try {
                var stream = App.class.getResourceAsStream(
                        "/cr/ac/una/astroline/resource/"
                        + empresa.getLogoPath().replace("assets/", ""));
                if (stream != null) {
                    logoEmpresa.setImage(new Image(stream));
                }
            } catch (Exception e) {
                System.err.println("[KioskoController] Logo no encontrado: " + e.getMessage());
            }
        }
    }

    
}