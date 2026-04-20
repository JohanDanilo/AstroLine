package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.App;
import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.service.EmpresaService;
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
import javafx.scene.layout.BorderPane;

public class AdminController extends Controller implements Initializable {

    private EmpresaService empresaService = EmpresaService.getInstancia();
    ;
    
    @FXML
    private BorderPane root;
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
        actualizarVista(empresaService.getEmpresa());

        empresaService.getEmpresaProperty().addListener((obs, oldEmp, newEmp) -> {
            actualizarVista(newEmp);
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    @FXML
    private void onActionBtnVerClientes(ActionEvent event) {
        FlowController.getInstance().goView("VerClienteView");
    }

    @FXML
    private void onActionBtnGeneral(ActionEvent event) {
        FlowController.getInstance().goView("MantenimientoParametrosGeneralesView");
    }

    @FXML
    private void onBtnConfigEstacion(ActionEvent event) {
        FlowController.getInstance().goView("ConfiguracionView");
    }

    @FXML
    private void onBtnTramites(ActionEvent event) {
        FlowController.getInstance().goView("MantenimientoTramitesView");
    }

    @FXML
    private void onBtnSucursales(ActionEvent event) {
        FlowController.getInstance().goView("MantenimientoSucursalView");
    }

    @FXML
    private void onBtnRankings(ActionEvent event) {
        FlowController.getInstance().goView("EstadisticasView");
    }

    private void actualizarVista(Empresa empresa) {
        if (empresa == null) {
            return;
        }
        nombreEmpresa.setText(empresa.getNombre());
        try {
            Image image = null;

            if (empresa.getLogoPath() != null && !empresa.getLogoPath().isBlank()) {

                String nombreSolo = new java.io.File(empresa.getLogoPath()).getName();
                java.io.File archivoLogo = new java.io.File("data/logoEmpresa/" + nombreSolo);

                if (archivoLogo.exists()) {
                    image = new Image(archivoLogo.toURI().toString(), true);
                }
            }

            if (image == null || image.isError()) {
                URL recurso = getClass().getResource("/cr/ac/una/astroline/resource/LogoEmpresa.png");

                if (recurso != null) {
                    image = new Image(recurso.toExternalForm(), true);
                }
            }

            logoEmpresa.setImage(null);
            logoEmpresa.setImage(image);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
