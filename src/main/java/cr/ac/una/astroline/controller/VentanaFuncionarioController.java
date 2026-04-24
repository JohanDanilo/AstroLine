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
import cr.ac.una.astroline.util.Mensaje;
import cr.ac.una.astroline.util.PathManager;
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
import javafx.scene.control.Button;
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

    @FXML private ImageView imagenCliente;
    @FXML private ImageView logoEmpresa;
    @FXML private MFXButton btnRegistroClientes;
    @FXML private Button btnSiguienteFicha;
    @FXML private Button btnRepetirFicha;
    @FXML private Button btnSiguientePreferencial;
    @FXML private MFXButton btnAusente;
    @FXML private Button btnSeleccionarFicha;
    @FXML private Label nombreEmpresa;
    @FXML private Label fichasEnEspera;
    @FXML private Label fichasTotalesEnEspera;
    @FXML private Label lblSucursal;
    @FXML private Label lblEstacion;
    @FXML private Label lblLetraFicha;
    @FXML private Label lblNumeroFicha;
    @FXML private Label lblNumeroCedula;
    @FXML private Label lblNombreCliente;
    @FXML private Label lblApellidosCliente;
    @FXML private Label lblValidacionPreferencial;
    @FXML private Label lblNombreTramiteCliente;

    private Ficha fichaActual;
    private final FichaService fichaService = FichaService.getInstancia();
    private Empresa empresa = EmpresaService.getInstancia().getEmpresa();
    private javafx.animation.Timeline actualizarContadorDeFichasEsperando;

    @Override
    public void initialize() {
        setNombreVista("Funcionario");
        actualizarContadorDeFichasEnEspera();
        cargarEmpresa();

        actualizarContadorDeFichasEsperando = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1),
                        e -> actualizarContadorDeFichasEnEspera()));
        actualizarContadorDeFichasEsperando.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        actualizarContadorDeFichasEsperando.play();

        ConfiguracionService cfg = ConfiguracionService.getInstancia();
        actualizarLabelsEstacion();

        // FIX: Recuperar ficha que quedó en estado LLAMADA de una sesión anterior.
        // Esto evita que el contador cuente una ficha que no puede ser obtenida
        // por obtenerSiguienteFicha() (que solo filtra ESPERANDO).
        recuperarFichaHuerfana();
        if (fichaActual != null) {
            cargarFicha(fichaActual);
        }

        if (cfg.isPreferencial()) {
            btnSiguienteFicha.setDisable(true);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    // -------------------------------------------------------------------------
    // CIERRE LIMPIO
    // -------------------------------------------------------------------------

    /**
     * Sobrescribimos setStage() para enganchar el onCloseRequest en cuanto
     * FlowController asigne el stage.
     * Se consume el evento para que JavaFX no cierre la ventana por su cuenta
     * — el cierre lo controla el diálogo de confirmación.
     */
    @Override
    public void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null) {
            stage.setOnCloseRequest((WindowEvent e) -> {
                e.consume();
                manejarSalida(false);
            });
        }
    }

    private void manejarSalida(boolean esCerrarSesion) {
        String accion = esCerrarSesion ? "cerrar sesión" : "cerrar la aplicación";

        String contenido;
        if (fichaActual != null && fichaActual.getEstado() == Ficha.Estado.LLAMADA) {
            String codigoFicha = fichaService.getCodigoLetra(fichaActual)
                    + "-" + fichaActual.getNumeroFormateado();
            contenido = "La ficha " + codigoFicha + " será registrada como ATENDIDA.\n"
                      + "Esta acción cerrará su sesión activa.";
        } else {
            contenido = "Esta acción cerrará su sesión activa.";
        }

        boolean confirmado = new Mensaje().showConfirmation(
                "¿Desea " + accion + "?",
                getStage(),
                contenido,
                "Confirmar",
                "Cancelar"
        );

        if (!confirmado) return;

        if (fichaActual != null && fichaActual.getEstado() == Ficha.Estado.LLAMADA) {
            fichaService.actualizarEstado(fichaActual.getId(), Ficha.Estado.ATENDIDA);
            fichaActual.setEstado(Ficha.Estado.ATENDIDA);
        }

        ejecutarSalida(esCerrarSesion);
    }

    private void ejecutarSalida(boolean esCerrarSesion) {
        if (actualizarContadorDeFichasEsperando != null) {
            actualizarContadorDeFichasEsperando.stop();
        }

        if (esCerrarSesion) {
            FlowController.getInstance().goViewInWindow("LoginFuncionarioView");
            getStage().close();
        } else {
            getStage().close();
        }
    }

    // -------------------------------------------------------------------------
    // RECUPERACIÓN DE FICHA HUÉRFANA
    // -------------------------------------------------------------------------

    /**
     * Al iniciar, busca si existe una ficha en estado LLAMADA asignada a esta
     * estación. Si la encuentra, la restaura como fichaActual para que el
     * diálogo de salida pueda detectarla y los botones de siguiente la marquen
     * como ATENDIDA antes de llamar la próxima.
     */
    private void recuperarFichaHuerfana() {
        String sucursalId = ConfiguracionService.getInstancia().getSucursalId();
        String estacionId = ConfiguracionService.getInstancia().getEstacionId();

        Respuesta r = fichaService.obtenerFichasActivasPorSucursal(sucursalId);
        if (!r.getEstado()) return;

        List<Ficha> activas = (List<Ficha>) r.getResultado("lista");
        activas.stream()
                .filter(f -> f.getEstado() == Ficha.Estado.LLAMADA)
                .filter(f -> estacionId != null && estacionId.equals(f.getEstacionId()))
                .findFirst()
                .ifPresent(f -> fichaActual = f);
    }

    // -------------------------------------------------------------------------
    // CONTADORES
    // -------------------------------------------------------------------------

    private void actualizarContadorDeFichasEnEspera() {
        String sucursalId = ConfiguracionService.getInstancia().getSucursalId();

        Respuesta respuesta = fichaService.obtenerFichasActivasPorSucursal(sucursalId);
        if (!respuesta.getEstado()) {
            fichasTotalesEnEspera.setText("0");
            fichasEnEspera.setText("0");
            return;
        }

        List<Ficha> activas = (List<Ficha>) respuesta.getResultado("lista");
        ConfiguracionService cfg = ConfiguracionService.getInstancia();
        List<String> tramitesConfigurados = cfg.getTramitesConfigurados();
        boolean soloPreferencial = cfg.isPreferencial();

        long cantidad = activas.stream()
                .filter(Ficha::estaEsperando)
                .count();
        fichasTotalesEnEspera.setText(String.valueOf(cantidad));

        long filtradas = activas.stream()
                .filter(Ficha::estaEsperando)
                .filter(f -> tramitesConfigurados.isEmpty()
                        || tramitesConfigurados.contains(f.getTramiteId()))
                .filter(f -> !soloPreferencial || f.isPreferencial())
                .count();
        fichasEnEspera.setText(String.valueOf(filtradas));
    }

    // -------------------------------------------------------------------------
    // EMPRESA / LOGO
    // -------------------------------------------------------------------------

    private void cargarEmpresa() {
        if (empresa == null) return;

        nombreEmpresa.setText(empresa.getNombre());

        if (empresa.getLogoPath() != null && !empresa.getLogoPath().isBlank()) {
            try {
                String nombreSolo = new java.io.File(empresa.getLogoPath()).getName();
                java.io.File archivoLogo = PathManager.getDataPath()
                        .resolve("logoEmpresa").resolve(nombreSolo).toFile();

                if (archivoLogo.exists()) {
                    logoEmpresa.setImage(new Image(archivoLogo.toURI().toString()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // -------------------------------------------------------------------------
    // ACCIONES DE FICHA
    // -------------------------------------------------------------------------

    @FXML
    private void onRepetirFicha(ActionEvent event) {
        if (fichaActual == null) return;

        // FIX: usar la ficha devuelta por registrarLlamado para tener el estado
        // actualizado en memoria (LLAMADA), no el objeto original (ESPERANDO).
        Respuesta rLlamado = fichaService.registrarLlamado(
                fichaActual.getId(),
                fichaActual.getEstacionId() != null
                        ? fichaActual.getEstacionId()
                        : ConfiguracionService.getInstancia().getEstacionId());
        if (rLlamado.getEstado()) {
            Ficha actualizada = (Ficha) rLlamado.getResultado("ficha");
            if (actualizada != null) fichaActual = actualizada;
        }

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

    // -------------------------------------------------------------------------
    // SIGUIENTE FICHA (regular)
    // -------------------------------------------------------------------------

    @FXML
    private void onSiguienteFicha(ActionEvent event) {
        marcarFichaActualAtendida();
        Ficha siguiente = obtenerSiguienteFicha();

        if (siguiente == null) {
            limpiarLabels("Sin fichas en espera");
            actualizarContadorDeFichasEnEspera();
            return;
        }

        // FIX: usar la ficha devuelta por registrarLlamado para tener el estado
        // actualizado en memoria (LLAMADA), no el objeto original (ESPERANDO).
        Respuesta rLlamado = fichaService.registrarLlamado(
                siguiente.getId(),
                ConfiguracionService.getInstancia().getEstacionId());
        if (rLlamado.getEstado()) {
            Ficha actualizada = (Ficha) rLlamado.getResultado("ficha");
            if (actualizada != null) siguiente = actualizada;
        }

        cargarFicha(siguiente);
        actualizarContadorDeFichasEnEspera();
    }

    private Ficha obtenerSiguienteFicha() {
        String sucursalId = ConfiguracionService.getInstancia().getSucursalId();
        Respuesta respuesta = fichaService.obtenerFichasActivasPorSucursal(sucursalId);
        if (!respuesta.getEstado()) return null;

        List<String> tramitesConfigurados = ConfiguracionService.getInstancia().getTramitesConfigurados();
        List<Ficha> activas = (List<Ficha>) respuesta.getResultado("lista");

        return activas.stream()
                .filter(Ficha::estaEsperando)
                .filter(f -> tramitesConfigurados.isEmpty()
                        || tramitesConfigurados.contains(f.getTramiteId()))
                .findFirst()
                .orElse(null);
    }

    // -------------------------------------------------------------------------
    // SIGUIENTE FICHA PREFERENCIAL
    // -------------------------------------------------------------------------

    @FXML
    private void onSiguientePreferencial(ActionEvent event) {
        marcarFichaActualAtendida();
        Ficha siguiente = obtenerSiguienteFichaPreferencial();

        if (siguiente == null) {
            limpiarLabels("Sin fichas preferenciales");
            actualizarContadorDeFichasEnEspera();
            return;
        }

        // FIX: usar la ficha devuelta por registrarLlamado para tener el estado
        // actualizado en memoria (LLAMADA), no el objeto original (ESPERANDO).
        // FIX 2: cargarFicha() solo se llama una vez (antes estaba duplicado).
        Respuesta rLlamado = fichaService.registrarLlamado(
                siguiente.getId(),
                siguiente.getEstacionId() != null
                        ? siguiente.getEstacionId()
                        : ConfiguracionService.getInstancia().getEstacionId());
        if (rLlamado.getEstado()) {
            Ficha actualizada = (Ficha) rLlamado.getResultado("ficha");
            if (actualizada != null) siguiente = actualizada;
        }

        cargarFicha(siguiente);
        actualizarContadorDeFichasEnEspera();
    }

    private Ficha obtenerSiguienteFichaPreferencial() {
        String sucursalId = ConfiguracionService.getInstancia().getSucursalId();
        Respuesta respuesta = fichaService.obtenerFichasActivasPorSucursal(sucursalId);
        if (!respuesta.getEstado()) return null;

        List<String> tramitesConfigurados = ConfiguracionService.getInstancia().getTramitesConfigurados();
        List<Ficha> activas = (List<Ficha>) respuesta.getResultado("lista");

        return activas.stream()
                .filter(f -> f.estaEsperando() && f.isPreferencial())
                .filter(f -> tramitesConfigurados.isEmpty()
                        || tramitesConfigurados.contains(f.getTramiteId()))
                .findFirst()
                .orElse(null);
    }

    // -------------------------------------------------------------------------
    // DATOS DEL CLIENTE
    // -------------------------------------------------------------------------

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
        java.io.File archivo = PathManager.getDataPath().resolve("fotos").resolve(nombreSolo).toFile();

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

    // -------------------------------------------------------------------------
    // LABELS / ESTADO
    // -------------------------------------------------------------------------

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

        lblSucursal.setText(sucursal != null ? sucursal.getNombre()
                : (sucursalId != null ? sucursalId : "-"));
        lblEstacion.setText(estacion != null ? estacion.getNombre()
                : (estacionId != null ? estacionId : "-"));
    }

    // -------------------------------------------------------------------------
    // AUSENTE / ATENDIDA
    // -------------------------------------------------------------------------

    @FXML
    private void onMarcarClienteAusente(ActionEvent event) {
        if (fichaActual == null) return;

        fichaService.actualizarEstado(fichaActual.getId(), Ficha.Estado.AUSENTE);
        fichaActual.setEstado(Ficha.Estado.AUSENTE);

        limpiarLabels("Cliente ausente");
        actualizarContadorDeFichasEnEspera();
    }

    public void marcarFichaActualAtendida() {
        if (fichaActual != null && fichaActual.getEstado() == Ficha.Estado.LLAMADA) {
            fichaService.actualizarEstado(fichaActual.getId(), Ficha.Estado.ATENDIDA);
            fichaActual.setEstado(Ficha.Estado.ATENDIDA);
        }
    }

    // -------------------------------------------------------------------------
    // SELECCIONAR FICHA (ventana secundaria)
    // -------------------------------------------------------------------------

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
            java.util.logging.Logger.getLogger(VentanaFuncionarioController.class.getName())
                    .log(Level.SEVERE, "Error abriendo FuncionarioSeleccionarFichaView.", ex);
        }
    }

    // -------------------------------------------------------------------------
    // NAVEGACIÓN
    // -------------------------------------------------------------------------

    @FXML
    private void onRegistroClientes(ActionEvent event) {
        FlowController.getInstance().goViewInWindow("VerClienteView");
    }

    @FXML
    private void onCerrarSesion(ActionEvent event) {
        manejarSalida(true);
    }
}