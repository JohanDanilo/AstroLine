package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.util.FlowController;
import io.github.palexdev.materialfx.controls.MFXButton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

/**
 * Controller placeholder del módulo Administrador.
 * Será implementado por José.
 *
 */
public class AdminController extends Controller implements Initializable {

    @FXML
    private MFXButton btnConfiguracionGeneral;
    @FXML
    private MFXButton btnTramites;
    @FXML
    private MFXButton btnRankings;
    @FXML
    private MFXButton btnClientes1;
    
    @FXML
    private MFXButton btnConfiguracion;

    @Override
    public void initialize() {
        setNombreVista("Administrador");
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

/*
    @FXML
    private void onBtnConfiguracion(ActionEvent event) {
        FlowController.getInstance().goView("ConfiguracionView");
    }
    
    @FXML
    private void onActionBtnClientes(ActionEvent event) {
        FlowController.getInstance().goView("MantenimientoClientesView");
    }
*/
    @FXML
    private void onActionBtnVerClientes(ActionEvent event) {
        FlowController.getInstance().goView("VerClienteView");
    }

    @FXML
    private void onActionBtnGeneral (ActionEvent event) {
        FlowController.getInstance().goView("MantenimientoParametrosGeneralesView");
    }
    
}