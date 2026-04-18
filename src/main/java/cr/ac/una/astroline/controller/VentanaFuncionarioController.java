
package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.App;
import cr.ac.una.astroline.model.Cliente;
import cr.ac.una.astroline.model.Ficha;
import cr.ac.una.astroline.service.ClienteService;
import cr.ac.una.astroline.service.FichaService;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.Respuesta;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.css.themes.MFXThemeManager;
import io.github.palexdev.materialfx.css.themes.Themes;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * FXML Controller class
 *
 * @author USUARIO UNA PZ
 */
public class VentanaFuncionarioController extends Controller implements Initializable {

    @FXML
    private MFXButton btnRegistroClientes;
    @FXML
    private MFXButton btnSiguienteFicha;
    @FXML
    private MFXButton btnRepetirFicha;
    @FXML
    private MFXButton btnSiguientePreferencial;
    @FXML
    private Label nombreEmpresa;
    @FXML
    private Label lblSucursal;
    @FXML
    private Label lblEstacion;
    @FXML
    private Label lblLetraFicha;
    @FXML
    private Label lblNumeroFicha;
    @FXML
    private Label lblNumeroCedula;
    @FXML
    private Label lblNombreCliente;
    @FXML
    private Label lblApellidosCliente;
    @FXML
    private Label lblValidacionPreferencial;
    @FXML
    private Label lblNombreTramiteCliente;

    private Ficha fichaActual;
    private final FichaService fichaService = new FichaService();

    @Override
    public void initialize() {
        setNombreVista("Funcionario");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    // -------------------------------------------------------------------------
    // REPETIR FICHA
    // -------------------------------------------------------------------------

    @FXML
    private void onRepetirFicha(ActionEvent event) {
        if (fichaActual == null) return;

        fichaActual.registrarLlamado(fichaActual.getEstacionId() != null
                ? fichaActual.getEstacionId() : "EST-01");
        fichaService.actualizarEstado(fichaActual.getId(), Ficha.Estado.LLAMADA);

        cargarFicha(fichaActual);
    }

    // -------------------------------------------------------------------------
    // CARGA DE FICHA
    // -------------------------------------------------------------------------

    /**
     * Recibe la ficha llamada y actualiza todos los labels de la vista.
     */
    public void cargarFicha(Ficha ficha) {
        this.fichaActual = ficha;

        lblLetraFicha.setText(fichaService.getCodigoLetra(ficha));
        lblNumeroFicha.setText(ficha.getNumeroFormateado());
        lblNombreTramiteCliente.setText(fichaService.getNombreTramite(ficha));
        lblSucursal.setText(ficha.getSucursalId());
        lblEstacion.setText(ficha.getEstacionId() != null ? ficha.getEstacionId() : "-");
        lblValidacionPreferencial.setText(ficha.isPreferencial() ? "Preferencial" : "Regular");

        String cedula = ficha.getCedulaCliente();
        if (cedula != null && !cedula.isBlank()) {
            cargarDatosCliente(cedula);
        } else {
            lblNumeroCedula.setText("No identificado");
            lblNombreCliente.setText("-");
            lblApellidosCliente.setText("-");
        }
    }

    /**
     * Busca el cliente por cédula y rellena los labels de nombre y apellidos.
     */
    private void cargarDatosCliente(String cedula) {
        lblNumeroCedula.setText(cedula);

        Cliente cliente = ClienteService.getInstancia().buscarPorCedula(cedula);

        if (cliente != null && !cliente.isEliminado()) {
            lblNombreCliente.setText(cliente.getNombre());
            lblApellidosCliente.setText(cliente.getApellidos());
        } else {
            lblNombreCliente.setText("-");
            lblApellidosCliente.setText("-");
        }
    }

    // -------------------------------------------------------------------------
    // SIGUIENTE FICHA
    // -------------------------------------------------------------------------

    @FXML
    private void onSiguienteFicha(ActionEvent event) {
        Ficha siguiente = obtenerSiguienteFicha();

        if (siguiente == null) {
            limpiarLabels("Sin fichas en espera");
            return;
        }

        siguiente.registrarLlamado("EST-01");
        fichaService.actualizarEstado(siguiente.getId(), Ficha.Estado.LLAMADA);

        cargarFicha(siguiente);
    }

    private Ficha obtenerSiguienteFicha() {
        Respuesta respuesta = fichaService.obtenerFichasActivas();
        if (!respuesta.getEstado()) return null;

        List<Ficha> activas = (List<Ficha>) respuesta.getResultado("lista");
        return activas.stream()
                .filter(Ficha::estaEsperando)
                .findFirst()
                .orElse(null);
    }

    // -------------------------------------------------------------------------
    // SIGUIENTE PREFERENCIAL
    // -------------------------------------------------------------------------

    @FXML
    private void onSiguientePreferencial(ActionEvent event) {
        Ficha siguiente = obtenerSiguienteFichaPreferencial();

        if (siguiente == null) {
            limpiarLabels("Sin fichas preferenciales");
            return;
        }

        siguiente.registrarLlamado(siguiente.getEstacionId() != null
                ? siguiente.getEstacionId() : "EST-01");
        fichaService.actualizarEstado(siguiente.getId(), Ficha.Estado.LLAMADA);

        cargarFicha(siguiente);
    }

    private Ficha obtenerSiguienteFichaPreferencial() {
        Respuesta respuesta = fichaService.obtenerFichasActivas();
        if (!respuesta.getEstado()) return null;

        List<Ficha> activas = (List<Ficha>) respuesta.getResultado("lista");
        return activas.stream()
                .filter(f -> f.estaEsperando() && f.isPreferencial())
                .findFirst()
                .orElse(null);
    }

    // -------------------------------------------------------------------------
    // LIMPIAR LABELS
    // -------------------------------------------------------------------------

    private void limpiarLabels(String mensajeFicha) {
        fichaActual = null;
        lblLetraFicha.setText("-");
        lblNumeroFicha.setText(mensajeFicha);
        lblNombreTramiteCliente.setText("-");
        lblSucursal.setText("-");
        lblEstacion.setText("-");
        lblValidacionPreferencial.setText("-");
        lblNumeroCedula.setText("-");
        lblNombreCliente.setText("-");
        lblApellidosCliente.setText("-");
    }

    // -------------------------------------------------------------------------
    // NAVEGACIÓN
    // -------------------------------------------------------------------------

    /**
     * Abre la ventana de selección de ficha e inyecta este controller como
     * padre para que pueda recibir la ficha elegida de vuelta.
     */
    @FXML
    private void onSeleccionarFicha(ActionEvent event) {
         try {
            FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/cr/ac/una/astroline/view/FuncionarioSeleccionarFichaView.fxml")
            );
            Parent root = loader.load();
 
            // Inyectar referencia a este controller ANTES de mostrar la ventana
            FuncionarioSeleccionarFichaController hijo = loader.getController();
            hijo.setControllerPadre(this);
            hijo.initialize();
 
            Stage stage = new Stage();
            stage.getIcons().add(new Image(
                App.class.getResourceAsStream("/cr/ac/una/astroline/resource/logo.png")
            ));
            stage.setTitle("Seleccionar Ficha");
            stage.setOnHidden((WindowEvent e) -> hijo.setStage(null));
            hijo.setStage(stage);
 
            Scene scene = new Scene(root);
            MFXThemeManager.addOn(scene, Themes.DEFAULT, Themes.LEGACY);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
 
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(VentanaFuncionarioController.class.getName())
                .log(Level.SEVERE, "Error abriendo FuncionarioSeleccionarFichaView.", ex);
        }
    
       // FlowController.getInstance().goViewInWindow("FuncionarioSeleccionarFichaView");
    }

    @FXML
    private void onRegistroClientes(ActionEvent event) {
        FlowController.getInstance().goViewInWindow("VerClienteView");
    }

    @FXML
    private void onCerrarSesion(ActionEvent event) {
        FlowController.getInstance().goViewInWindow("LoginFuncionarioView");
        getStage().close();
    }
}
//
//import cr.ac.una.astroline.model.Ficha;
//import cr.ac.una.astroline.service.FichaService;
//import cr.ac.una.astroline.util.FlowController;
//import cr.ac.una.astroline.util.Respuesta;
//import io.github.palexdev.materialfx.controls.MFXButton;
//import io.github.palexdev.materialfx.controls.MFXComboBox;
//import java.net.URL;
//import java.util.List;
//import java.util.ResourceBundle;
//import java.util.stream.Collectors;
//import javafx.collections.FXCollections;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//
///**
// *
// * @author USUARIO UNA PZ
// */
//
//public class FuncionarioSeleccionarFichaController extends Controller implements Initializable {
//
//    @FXML
//    private MFXComboBox<Ficha> cmbFichas;
//    @FXML
//    private MFXButton btnLlamarFichaSeleccionada ;
//    
//    private final FichaService fichaService = new FichaService();
//    
//    @Override
//    public void initialize() {
//    setNombreVista("FuncionarioSeleccionarFicha");
//        cargarFichasEnEspera();
//    }
//
//    @Override
//    public void initialize(URL url, ResourceBundle rb) {}
//
//    
//    private void cargarFichasEnEspera() {
//        Respuesta respuesta = fichaService.obtenerFichasActivas();
//        if (!respuesta.getEstado()) return;
// 
//        List<Ficha> activas = (List<Ficha>) respuesta.getResultado("lista");
//        List<Ficha> enEspera = activas.stream()
//                .filter(Ficha::estaEsperando)
//                .collect(Collectors.toList());
// 
//        cmbFichas.setItems(FXCollections.observableArrayList(enEspera));
// 
//        // Muestra texto legible en el combo usando el código + trámite + tipo
//        cmbFichas.setConverter(new javafx.util.StringConverter<Ficha>() {
//            @Override
//            public String toString(Ficha ficha) {
//                if (ficha == null) return "";
//                String tipo = ficha.isPreferencial() ? " ★ Preferencial" : "";
//                return ficha.getCodigo() + " | " + ficha.getTramiteId() + tipo;
//            }
// 
//            @Override
//            public Ficha fromString(String string) {
//                return null; // No se necesita conversión inversa
//            }
//        });
//    }
//    @FXML
//    private void OnLlamarFichaSeleccionada(ActionEvent event) {
//        Ficha seleccionada = cmbFichas.getValue();
// 
//        if (seleccionada == null) {
//            return; // Nada seleccionado, no hace nada
//        }
// 
//        // Registrar llamado y persistir estado
//        seleccionada.registrarLlamado(seleccionada.getEstacionId() != null
//                ? seleccionada.getEstacionId() : "EST-01");
//        fichaService.actualizarEstado(seleccionada.getId(), Ficha.Estado.LLAMADA);
// 
//        // Enviar la ficha a VentanaFuncionario y navegar hacia ella
//        VentanaFuncionarioController ventana =
//                (VentanaFuncionarioController) FlowController.getInstance()
//                        .getController("FuncionarioView");
// 
//        if (ventana != null) {
//            ventana.cargarFicha(seleccionada);
//        }
// 
//        // Cerrar esta ventana y volver a VentanaFuncionario
//        FlowController.getInstance().goViewInWindow("FuncionarioView");
//        getStage().close();
//    }
//    
//    
//}