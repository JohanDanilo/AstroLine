
package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Cliente;
import cr.ac.una.astroline.model.Ficha;
import cr.ac.una.astroline.service.ClienteService;
import cr.ac.una.astroline.service.FichaService;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.Respuesta;
import io.github.palexdev.materialfx.controls.MFXButton;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
 
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
        setNombreVista("VentanaFuncionario");
    }
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {}
 
    // -------------------------------------------------------------------------
    // CARGA DE FICHA
    // -------------------------------------------------------------------------
 
    /**
     * Recibe la ficha llamada desde FuncionarioSeleccionarFichaController
     * y actualiza todos los labels de la vista.
     */
    public void cargarFicha(Ficha ficha) {
        this.fichaActual = ficha;
 
        lblLetraFicha.setText(ficha.getTramiteId());
        lblNumeroFicha.setText(ficha.getNumeroFormateado());
        lblNombreTramiteCliente.setText(ficha.getTramiteId());
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
 
    /**
     * Llama automáticamente la siguiente ficha en espera (por orden de llegada).
     * Si no hay fichas, limpia los labels e informa al funcionario.
     */
    @FXML
    private void onSiguienteFicha(ActionEvent event) {
        Ficha siguiente = obtenerSiguienteFicha();
 
        if (siguiente == null) {
            limpiarLabels("Sin fichas en espera");
            return;
        }
 
        // Reemplaza "EST-01" con el id de estación real del funcionario logueado
        siguiente.registrarLlamado("EST-01");
        fichaService.actualizarEstado(siguiente.getId(), Ficha.Estado.LLAMADA);
 
        cargarFicha(siguiente);
    }
 
    /**
     * Obtiene la primera ficha con estado ESPERANDO de la lista activa.
     * El orden de llegada está garantizado por el índice en fichas.json.
     */
    private Ficha obtenerSiguienteFicha() {
        Respuesta respuesta = fichaService.obtenerFichasActivas();
        if (!respuesta.getEstado()) return null;
 
        List<Ficha> activas = (List<Ficha>) respuesta.getResultado("lista");
        return activas.stream()
                .filter(Ficha::estaEsperando)
                .findFirst()
                .orElse(null);
    }
 
    /**
     * Limpia todos los labels de ficha y cliente.
     *
     * @param mensajeFicha texto a mostrar en lblNumeroFicha
     */
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
 
    @FXML
    private void onBtnSeleccionarFicha(ActionEvent event) {
        FlowController.getInstance().goViewInWindow("FuncionarioSeleccionarFichaView");
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
//import io.github.palexdev.materialfx.controls.MFXButton;
//import java.net.URL;
//import java.util.ResourceBundle;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.scene.layout.AnchorPane;
//import javafx.fxml.Initializable;
//import cr.ac.una.astroline.util.FlowController;
//import cr.ac.una.astroline.model.Ficha;
//import cr.ac.una.astroline.service.FichaService;
//import cr.ac.una.astroline.util.Respuesta;
//import java.util.List;
//import javafx.scene.control.Label;
//
///**
// * FXML Controller class
// *
// * @author USUARIO UNA PZ
// */
//public class VentanaFuncionarioController extends Controller implements Initializable {
//    
//    @FXML
//    private MFXButton btnRegistroClientes;
//    @FXML
//    private MFXButton btnSiguienteFicha;
//    @FXML
//    private Label nombreEmpresa;
//    @FXML
//    private Label lblSucursal;
//    @FXML
//    private Label lblEstacion;
//    @FXML
//    private Label lblLetraFicha;
//    @FXML
//    private Label lblNumeroFicha;
//    @FXML
//    private Label lblNumeroCedula;
//    @FXML
//    private Label lblNombreCliente;
//    @FXML
//    private Label lblApellidosCliente;
//    @FXML
//    private Label lblValidacionPreferencial;
//    @FXML
//    private Label lblNombreTramiteCliente;
//    private Ficha fichaActual; 
//    private final FichaService fichaService = new FichaService();
//   
//    @Override
//    public void initialize() {
//        setNombreVista("VentanaFuncionario");
//    }
//    
//    @Override
//    public void initialize(URL url, ResourceBundle rb) {
//        // TODO
//    }    
//    
//    @FXML
//    private void onBtnSeleccionarFicha(ActionEvent event) {
//        FlowController.getInstance().goViewInWindow("FuncionarioSeleccionarFichaView");
//    }
//
//    @FXML
//    private void onRegistroClientes(ActionEvent event) {
//         FlowController.getInstance().goViewInWindow("VerClienteView");
//    }
//
//    @FXML
//    private void onCerrarSesion(ActionEvent event) {
//        FlowController.getInstance().goViewInWindow("LoginFuncionarioView");
//        getStage().close();
//    }
//    
//    /**
// * Llama automáticamente la siguiente ficha en espera (por orden de llegada).
// * Si no hay fichas, limpia los labels e informa al funcionario.
// */
//@FXML
//private void onSiguienteFicha(ActionEvent event) {
//    Ficha siguiente = obtenerSiguienteFicha();
//
//    if (siguiente == null) {
//        limpiarLabels("Sin fichas en espera");
//        return;
//    }
//
//    // Registrar llamado: cambia estado a LLAMADA y guarda fecha/hora
//    // Reemplaza "EST-01" con el id de estación real del funcionario logueado
//    siguiente.registrarLlamado("EST-01");
//    fichaService.actualizarEstado(siguiente.getId(), Ficha.Estado.LLAMADA);
//
//    cargarFicha(siguiente);
//}
//
///**
// * Obtiene la primera ficha con estado ESPERANDO de la lista activa.
// * El orden de llegada está garantizado por el índice en fichas.json
// * (cada ficha nueva se agrega al final de la lista).
// *
// * @return la ficha más antigua en espera, o null si no hay ninguna
// */
//private Ficha obtenerSiguienteFicha() {
//    Respuesta respuesta = fichaService.obtenerFichasActivas();
//    if (!respuesta.getEstado()) return null;
//
//    List<Ficha> activas = (List<Ficha>) respuesta.getResultado("lista");
//    return activas.stream()
//            .filter(Ficha::estaEsperando)
//            .findFirst()
//            .orElse(null);
//}
//
///**
// * Limpia todos los labels de ficha y cliente.
// *
// * @param mensajeFicha texto a mostrar en lblNumeroFicha (ej: "Sin fichas en espera")
// */
//private void limpiarLabels(String mensajeFicha) {
//    fichaActual = null;
//    lblLetraFicha.setText("-");
//    lblNumeroFicha.setText(mensajeFicha);
//    lblNombreTramiteCliente.setText("-");
//    lblSucursal.setText("-");
//    lblEstacion.setText("-");
//    lblValidacionPreferencial.setText("-");
//    lblNumeroCedula.setText("-");
//    lblNombreCliente.setText("-");
//    lblApellidosCliente.setText("-");
//}
//}
