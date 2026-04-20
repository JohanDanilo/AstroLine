package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.App;
import cr.ac.una.astroline.model.Cliente;
import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.model.Estacion;
import cr.ac.una.astroline.model.Ficha;
import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.service.ClienteService;
import cr.ac.una.astroline.service.FichaService;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.Respuesta;
import cr.ac.una.astroline.service.ConfiguracionService;
import cr.ac.una.astroline.service.EmpresaService;
import cr.ac.una.astroline.service.SucursalService;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.css.themes.MFXThemeManager;
import io.github.palexdev.materialfx.css.themes.Themes;
import java.io.File;
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
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * FXML Controller class
 *
 * @author USUARIO UNA PZ
 */
public class VentanaFuncionarioController extends Controller implements Initializable {

    @FXML
    private ImageView imagenCliente;
    @FXML
    private ImageView logoEmpresa;
    @FXML
    private MFXButton btnRegistroClientes;
    @FXML
    private MFXButton btnSiguienteFicha;
    @FXML
    private MFXButton btnRepetirFicha;
    @FXML
    private MFXButton btnSiguientePreferencial;
    @FXML
    private MFXButton btnAusente;
    @FXML
    private MFXButton btnSeleccionarFicha;
    @FXML
    private Label nombreEmpresa;
    @FXML
    private Label fichasEnEspera;
    @FXML
    private Label fichasTotalesEnEspera;
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
    private final FichaService fichaService = FichaService.getInstancia();
    private Empresa empresa = EmpresaService.getInstancia().getEmpresa();
    ;
    private javafx.animation.Timeline actualizarContadorDeFichasEsperando;

    @Override
    public void initialize() {
        setNombreVista("Funcionario");
        actualizarContadorDeFichasEnEspera();
        cargarEmpresa();

        actualizarContadorDeFichasEsperando = new javafx.animation.Timeline(new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> actualizarContadorDeFichasEnEspera()));
        actualizarContadorDeFichasEsperando.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        actualizarContadorDeFichasEsperando.play();

        ConfiguracionService cfg = ConfiguracionService.getInstancia();
        actualizarLabelsEstacion();

        if (cfg.isPreferencial()) {
            btnSiguienteFicha.setDisable(true);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    private void actualizarContadorDeFichasEnEspera() {
        Respuesta respuesta = fichaService.obtenerFichasActivas();
        if (!respuesta.getEstado()) {
            fichasTotalesEnEspera.setText("0");
            fichasEnEspera.setText("0");
            return;
        }

        List<Ficha> activas = (List<Ficha>) respuesta.getResultado("lista");
        List<String> tramitesConfigurados = ConfiguracionService.getInstancia().getTramitesConfigurados();
        
        long cantidad = activas.stream().filter(Ficha::estaEsperando).count();
        fichasTotalesEnEspera.setText(String.valueOf(cantidad));
        
        long filtradas = activas.stream()
            .filter(Ficha::estaEsperando)
            .filter(f -> tramitesConfigurados.isEmpty() || tramitesConfigurados.contains(f.getTramiteId()))
            .count();
        fichasEnEspera.setText(String.valueOf(filtradas));
    }

    private void cargarEmpresa() {

        if (empresa == null) {
            return;
        }

        nombreEmpresa.setText(empresa.getNombre());

        if (empresa.getLogoPath() != null && !empresa.getLogoPath().isBlank()) {
            try {
                String nombreSolo = new java.io.File(empresa.getLogoPath()).getName();
                java.io.File archivoLogo = new java.io.File("data/logoEmpresa/" + nombreSolo);

                if (archivoLogo.exists()) {
                    logoEmpresa.setImage(new Image(archivoLogo.toURI().toString()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @FXML
    private void onRepetirFicha(ActionEvent event) {
        if (fichaActual == null) {
            return;
        }

        fichaService.registrarLlamado(fichaActual.getId(),
        fichaActual.getEstacionId() != null
        ? fichaActual.getEstacionId() : ConfiguracionService.getInstancia().getEstacionId());

        cargarFicha(fichaActual);
    }

    public void cargarFicha(Ficha ficha) {
        this.fichaActual = ficha;

        lblLetraFicha.setText(fichaService.getCodigoLetra(ficha));
        lblNumeroFicha.setText(ficha.getNumeroFormateado());
        lblNombreTramiteCliente.setText(fichaService.getNombreTramite(ficha));
        lblValidacionPreferencial.setText(ficha.isPreferencial() ? "Preferencial" : "Regular");

        actualizarLabelsEstacion();

        String cedula = ficha.getCedulaCliente();
        if (cedula != null && !cedula.isBlank()) {
            cargarDatosCliente(cedula);
        } else {
            lblNumeroCedula.setText("No identificado");
            lblNombreCliente.setText("-");
            lblApellidosCliente.setText("-");
            limpiarFotoCliente();
        }
    }

    private void cargarDatosCliente(String cedula) {
        lblNumeroCedula.setText(cedula);

        Cliente cliente = ClienteService.getInstancia().buscarPorCedula(cedula);

        if (cliente != null && !cliente.isEliminado()) {
            lblNombreCliente.setText(cliente.getNombre());
            lblApellidosCliente.setText(cliente.getApellidos());
            cargarFotoCliente(cliente.getFotoPath());
        } else {
            lblNombreCliente.setText("-");
            lblApellidosCliente.setText("-");
            limpiarFotoCliente();
        }
    }

    private void cargarFotoCliente(String fotoPath) {
        if (fotoPath == null || fotoPath.isBlank()) {
            limpiarFotoCliente();
            return;
        }

        String nombreSolo = new java.io.File(fotoPath).getName();
        java.io.File archivo = new java.io.File("data/fotos/" + nombreSolo);

        if (archivo.exists()) {
            try {
                Image imagen = new Image(archivo.toURI().toString(), true);
                imagenCliente.setImage(imagen);
                return;
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(VentanaFuncionarioController.class.getName())
                    .log(Level.WARNING, "No se pudo cargar la foto: " + archivo.getAbsolutePath(), ex);
            }
        }

        limpiarFotoCliente();
    }

    private void limpiarFotoCliente() {
        imagenCliente.setImage(null);
    }

    @FXML
    private void onSiguienteFicha(ActionEvent event) {
        marcarFichaActualAtendida();
        Ficha siguiente = obtenerSiguienteFicha();

        if (siguiente == null) {
            limpiarLabels("Sin fichas en espera");
            actualizarContadorDeFichasEnEspera();
            return;
        }

        fichaService.registrarLlamado(siguiente.getId(), ConfiguracionService.getInstancia().getEstacionId());

        cargarFicha(siguiente);
        actualizarContadorDeFichasEnEspera();
    }

    private Ficha obtenerSiguienteFicha() {
        Respuesta respuesta = fichaService.obtenerFichasActivas();
        if (!respuesta.getEstado()) {
            return null;
        }

        List<String> tramitesConfigurados = ConfiguracionService.getInstancia().getTramitesConfigurados();

        List<Ficha> activas = (List<Ficha>) respuesta.getResultado("lista");
        return activas.stream()
                .filter(Ficha::estaEsperando)
                .filter(f -> tramitesConfigurados.isEmpty() || tramitesConfigurados.contains(f.getTramiteId()))
                .findFirst()
                .orElse(null);
    }

    @FXML
    private void onSiguientePreferencial(ActionEvent event) {
        marcarFichaActualAtendida();
        Ficha siguiente = obtenerSiguienteFichaPreferencial();

        if (siguiente == null) {
            limpiarLabels("Sin fichas preferenciales");
            actualizarContadorDeFichasEnEspera();
            return;
        }

        fichaService.registrarLlamado(siguiente.getId(),
        siguiente.getEstacionId() != null
        ? siguiente.getEstacionId() : ConfiguracionService.getInstancia().getEstacionId());
        cargarFicha(siguiente);
        actualizarContadorDeFichasEnEspera();
    }

    private Ficha obtenerSiguienteFichaPreferencial() {
        Respuesta respuesta = fichaService.obtenerFichasActivas();
        if (!respuesta.getEstado()) {
            return null;
        }
        List<String> tramitesConfigurados = ConfiguracionService.getInstancia().getTramitesConfigurados();

        List<Ficha> activas = (List<Ficha>) respuesta.getResultado("lista");
        return activas.stream()
                .filter(f -> f.estaEsperando() && f.isPreferencial())
                .filter(f -> tramitesConfigurados.isEmpty() || tramitesConfigurados.contains(f.getTramiteId()))
                .findFirst()
                .orElse(null);
    }

    private void limpiarLabels(String mensajeFicha) {
        fichaActual = null;
        lblLetraFicha.setText("-");
        lblNumeroFicha.setText(mensajeFicha);
        lblNombreTramiteCliente.setText("-");
        lblValidacionPreferencial.setText("-");
        lblNumeroCedula.setText("-");
        lblNombreCliente.setText("-");
        lblApellidosCliente.setText("-");
        actualizarLabelsEstacion();
        limpiarFotoCliente();
    }
    
    private void actualizarLabelsEstacion() {
    ConfiguracionService cfg = ConfiguracionService.getInstancia();
    SucursalService ss = SucursalService.getInstancia();

    String sucursalId = cfg.getSucursalId();
    String estacionId = cfg.getEstacionId();

    Sucursal sucursal = ss.buscarSucursal(sucursalId);
    Estacion estacion = ss.buscarEstacion(estacionId);

    lblSucursal.setText(sucursal != null ? sucursal.getNombre() : (sucursalId != null ? sucursalId : "-"));
    lblEstacion.setText(estacion != null ? estacion.getNombre() : (estacionId != null ? estacionId : "-"));
}

    @FXML
    private void onSeleccionarFicha(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    App.class.getResource("/cr/ac/una/astroline/view/FuncionarioSeleccionarFichaView.fxml")
            );
            Parent root = loader.load();

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
            java.util.logging.Logger.getLogger(VentanaFuncionarioController.class.getName()).log(Level.SEVERE, "Error abriendo FuncionarioSeleccionarFichaView.", ex);
        }

    }

    @FXML
    private void onMarcarClienteAusente(ActionEvent event) {
        if (fichaActual == null) {
            return;
        }

        fichaService.actualizarEstado(fichaActual.getId(), Ficha.Estado.AUSENTE);
        fichaActual.setEstado(Ficha.Estado.AUSENTE);

        limpiarLabels("Cliente ausente");
        actualizarContadorDeFichasEnEspera();
    }

    private void marcarFichaActualAtendida() {
        if (fichaActual != null && fichaActual.getEstado() == Ficha.Estado.LLAMADA) {
            fichaService.actualizarEstado(fichaActual.getId(), Ficha.Estado.ATENDIDA);
            fichaActual.setEstado(Ficha.Estado.ATENDIDA);
        }
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
