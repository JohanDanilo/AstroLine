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
    private MFXButton btnClientes;
    @FXML
    private MFXButton btnTramites;
    @FXML
    private MFXButton btnRankings;
    @FXML
    private MFXButton btnSucursal;

    @Override
    public void initialize() {
        setNombreVista("Administrador");
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    

    @FXML
    private void onActionBtnClientes(ActionEvent event) {
        FlowController.getInstance().goView("MantenimientoClientesView");
    }

    @FXML
    private void onActionBtnTramites(ActionEvent event) {
    }

    @FXML
    private void onActionBtnRankings(ActionEvent event) {
    }

    @FXML
    private void onActionBtnSucursal(ActionEvent event) {
        FlowController.getInstance().goView("MantenimientoSucursalView");
    }
    
}