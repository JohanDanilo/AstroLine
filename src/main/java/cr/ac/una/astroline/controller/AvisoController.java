/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.util.FlowController;
import io.github.palexdev.materialfx.controls.MFXButton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

/**
 * FXML Controller class
 *
 * @author takka_sama
 */
public class AvisoController extends Controller implements Initializable {

    @FXML
    private Label lblAviso;
    @FXML
    private MFXButton btnAceptar;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) { }    
    @Override
    public void initialize() { }
    @FXML
    private void onActionBtnAceptar(ActionEvent event) {
        FlowController.getInstance().getController("AvisoView").getStage().close();
    }
    
    public void cambiarInformacionDeAviso(String msg){ this.lblAviso.setText(msg);}
    
}
