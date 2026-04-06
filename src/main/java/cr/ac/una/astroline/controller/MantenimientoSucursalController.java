/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Estacion;
import cr.ac.una.astroline.model.EstacionDTO;
import cr.ac.una.astroline.model.Tramite;
import cr.ac.una.astroline.util.FlowController;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author takka_sama
 */
public class MantenimientoSucursalController extends Controller implements Initializable {

    @FXML
    private Label lblSucursal;
    @FXML
    private MFXTextField txtBarraBusquedaEstaciones;
    @FXML
    private MFXButton btnAgregar;
    @FXML
    private ListView<EstacionDTO> listaEstaciones;
    @FXML
    private TableView<Tramite> tableTramitesAsignados;
    @FXML
    private TableView<Tramite> tableTramitesDiponibles;
    @FXML
    private TableColumn<Tramite, String> colTramiteAsignado;
    @FXML
    private TableColumn<Tramite, String> colEstadoAsignado;
    @FXML
    private TableColumn<Tramite, String> colTramiteDisponible;
    @FXML
    private TableColumn<Tramite, String> colEstadoDisponible;
    @FXML
    private AnchorPane root;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void onKeyPressedEstaciones(KeyEvent event) {
    }

    @FXML
    private void onMousePressedEstaciones(MouseEvent event) {
    }

    @FXML
    private void onDragEnteredAsignados(DragEvent event) {
    }

    @FXML
    private void onDragDetectedAsignados(MouseEvent event) {
    }

    @FXML
    private void onDragDoneAsignados(DragEvent event) {
    }

    @FXML
    private void onDragDroppedAsignados(DragEvent event) {
    }

    @FXML
    private void onDragEntered(DragEvent event) {
    }

    @FXML
    private void onDragDetectedDisponibles(MouseEvent event) {
    }

    @FXML
    private void onDragDoneDisponibles(DragEvent event) {
    }

    @FXML
    private void onDragDroppedDisponibles(DragEvent event) {
    }

    @FXML
    private void onActionBtnAgregar(ActionEvent event) {
        FlowController.getInstance().goViewInWindowModal("RegistroEstacionView", 
                FlowController.getInstance().getMainStage(), true);
    }

    @Override
    public void initialize() {
    }
    
}
