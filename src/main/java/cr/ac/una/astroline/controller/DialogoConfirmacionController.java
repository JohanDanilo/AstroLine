package cr.ac.una.astroline.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DialogoConfirmacionController extends Controller implements Initializable {

    @FXML private Label lblTitulo;
    @FXML private Label lblMensaje;
    @FXML private Button btnAceptar;
    @FXML private Button btnCancelar;

    private boolean resultado = false;

    public void initData(String titulo, String mensaje) {
        initData(titulo, mensaje, "Aceptar", "Cancelar");
    }

    public void initData(String titulo, String mensaje,
                         String textoAceptar, String textoCancelar) {
        lblTitulo.setText(titulo);
        lblMensaje.setText(mensaje);
        btnAceptar.setText(textoAceptar);
        btnCancelar.setText(textoCancelar);
    }

    @FXML
    private void onAceptar() {
        resultado = true;
        cerrar();
    }

    @FXML
    private void onCancelar() {
        resultado = false;
        cerrar();
    }

    public boolean getResultado() {
        return resultado;
    }

    private void cerrar() {
        Stage stage = (Stage) btnAceptar.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

}