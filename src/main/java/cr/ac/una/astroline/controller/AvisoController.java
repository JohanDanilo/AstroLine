package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.util.FlowController;
import io.github.palexdev.materialfx.controls.MFXButton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class AvisoController extends Controller implements Initializable {

    @FXML
    private Label lblAviso;
    @FXML
    private MFXButton btnAceptar;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @Override
    public void initialize() {
    }

    @FXML
    private void onActionBtnAceptar(ActionEvent event) {
        getStage().close();
    }

    public void cambiarInformacionDeAviso(String msg) {
        this.lblAviso.setText(msg);
    }

}
