package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Cliente;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.GsonUtil;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/*
*
*
*       encargada de la vista de registro del cliente
*
*
 */
public class RegistroClienteController extends Controller implements Initializable {

    @FXML
    private AnchorPane root;
    @FXML
    private MFXButton btnCargar;
    @FXML
    private MFXButton btnCamara;
    @FXML
    private MFXDatePicker dpFechaDeNacimiento;
    @FXML
    private MFXTextField txtNombre;
    @FXML
    private MFXTextField txtApellidos;
    @FXML
    private MFXTextField txtCedula;
    @FXML
    private MFXTextField txtCorreoElectronico;
    @FXML
    private MFXTextField txtTelefono;
    @FXML
    private MFXButton btnAgregarCliente;
    @FXML
    private MFXButton btnCancelar;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private Pane panePadre;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @Override
    public void initialize() {

    }

    @FXML
    private void onActionBtnCargar(ActionEvent event) {
    }

    @FXML
    private void onActionBtnCamara(ActionEvent event) {

    }

    @FXML
    private void onActionBtnAgregarCliente(ActionEvent event) {

        if (guardarCliente()) {
            limpiarVistaDeRegistro();
            FlowController.getInstance().goViewInDividePane("", panePadre, false);
        }

    }

    @FXML
    private void onActionBtnCancelar(ActionEvent event) {

        limpiarVistaDeRegistro();
        FlowController.getInstance().goViewInDividePane("", panePadre, false);


    }

    //Guardar y Verificar informacion del Cliente 
    private boolean guardarCliente() {
        if (!esValidoCliente()) {
            return false;
        }

        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setNombre(txtNombre.getText());
        nuevoCliente.setApellidos(txtApellidos.getText());
        nuevoCliente.setCedula(txtCedula.getText());
        nuevoCliente.setCorreo(txtCorreoElectronico.getText());
        nuevoCliente.setTelefono(txtTelefono.getText());
        nuevoCliente.setFechaNacimiento(dpFechaDeNacimiento.getValue().toString());

        GsonUtil.guardar(nuevoCliente, "clientes.json");

        return true;
    }

    private boolean esValidoCliente() {

        if (!esValidoNombreYApellidos()) {
            abrirAviso(" Revisar : Nombre y Apellidos ");
            return false;
        }
        if (!esValidaCedula()) {
            abrirAviso("Revisar : Cedula ");
            return false;
        }
        if (!esValidaFechaDeNacimiento()) {
            abrirAviso("Revisar : Fecha de Nacimiento");
            return false;
        }
        if (!esValidoCorreoElectronico()) {
            abrirAviso("Revisar : Correo Electronico ");
            return false;
        }
        if (!esValidoTelefono()) {
            abrirAviso("Revisar : Telefono ");
            return false;
        }

        return true;

    }

    private void abrirAviso(String msg) {

        Controller controller = FlowController.getInstance().getController("AvisoView");

        if (controller instanceof AvisoController avisoController) {
            avisoController.cambiarInformacionDeAviso(msg);
        }

        FlowController.getInstance().goViewInWindowModal("AvisoView", FlowController.getInstance().getMainStage(), true);

    }

    //Validaciones de espacios
    private boolean esValidoNombreYApellidos() {

        String formatoNombreYApellidos = "[A-Za-z\\s]{4,30}";

        return txtApellidos.getText().matches(formatoNombreYApellidos) && txtNombre.getText().matches(formatoNombreYApellidos);
    }

    private boolean esValidoCorreoElectronico() {

        String formantoCorreoElectronico = "^[a-zA-Z0-9._^\\-]{4,50}@[a-zA-Z.]{4,30}\\.[a-z]{2,3}$";
        return txtCorreoElectronico.getText().matches(formantoCorreoElectronico);

    }

    private boolean esValidoTelefono() {

        String formatoTelefono = "\\d{8}";
        return txtTelefono.getText().matches(formatoTelefono);

    }

    private boolean esValidaFechaDeNacimiento() {

        return dpFechaDeNacimiento.getValue() != null;

    }

    private boolean esValidaCedula() {

        String formatoCedula = "[\\d]*";
        return txtCedula.getText().matches(formatoCedula);

    }

    //Funciones de UI
    private void limpiarVistaDeRegistro() {
        txtApellidos.clear();
        txtNombre.clear();
        txtCedula.clear();
        txtTelefono.clear();
        txtCorreoElectronico.clear();
        dpFechaDeNacimiento.clear();
    }

    public void setPanePadre(Pane pane) {
        this.panePadre = pane;
    }
}
