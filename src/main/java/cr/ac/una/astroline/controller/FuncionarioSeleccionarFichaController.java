
package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Ficha;
import cr.ac.una.astroline.service.ConfiguracionService;
import cr.ac.una.astroline.service.FichaService;
import cr.ac.una.astroline.util.Respuesta;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

/**
 *
 * @author USUARIO UNA PZ
 */
public class FuncionarioSeleccionarFichaController extends Controller implements Initializable {

    @FXML
    private MFXComboBox<Ficha> cmbFichas;
    @FXML
    private MFXButton btnLlamarFichaSeleccionada;

    private final FichaService fichaService = new FichaService();

    /** Referencia al controller padre, inyectada antes de abrir esta ventana. */
    private VentanaFuncionarioController controllerPadre;

    @Override
    public void initialize() {
        setNombreVista("Seleccionar Ficha");
        cargarFichasEnEspera();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    /**
     * Debe llamarse desde VentanaFuncionarioController justo antes de abrir
     * esta ventana, para poder enviarle la ficha seleccionada de vuelta.
     */
    public void setControllerPadre(VentanaFuncionarioController padre) {
        this.controllerPadre = padre;
    }

    // -------------------------------------------------------------------------
    // CARGA DEL COMBO
    // -------------------------------------------------------------------------
    private void cargarFichasEnEspera() {
    Respuesta respuesta = fichaService.obtenerFichasActivas();
    if (!respuesta.getEstado()) return;

    ConfiguracionService cfg = ConfiguracionService.getInstancia();
    cfg.recargarConfiguracion(); // fuerza relectura del JSON por si acaso
    List<String> tramitesConfigurados = cfg.getTramitesConfigurados();
    boolean soloPreferencial = cfg.isPreferencial();

    List<Ficha> activas = (List<Ficha>) respuesta.getResultado("lista");

    List<Ficha> enEspera = activas.stream()
            .filter(Ficha::estaEsperando)
            .filter(f -> tramitesConfigurados.isEmpty() || tramitesConfigurados.contains(f.getTramiteId()))
            .filter(f -> !soloPreferencial || f.isPreferencial())
            .collect(Collectors.toList());

    System.out.println("[DEBUG] Fichas tras filtro: " + enEspera.size());

    cmbFichas.setItems(FXCollections.observableArrayList(enEspera));

    cmbFichas.setConverter(new javafx.util.StringConverter<Ficha>() {
        @Override
        public String toString(Ficha ficha) {
            if (ficha == null) return "";
            String tipo = ficha.isPreferencial() ? " | Preferencial" : "";
            return ficha.getCodigo() + " | " + ficha.getTramiteId() + tipo;
        }

        @Override
        public Ficha fromString(String string) { return null; }
    });
}


    // -------------------------------------------------------------------------
    // LLAMAR FICHA SELECCIONADA
    // -------------------------------------------------------------------------

    @FXML
    private void OnLlamarFichaSeleccionada(ActionEvent event) {
        Ficha seleccionada = cmbFichas.getValue();

        if (seleccionada == null) return;

        // Registrar llamado y persistir estado
        seleccionada.registrarLlamado(seleccionada.getEstacionId() != null
                ? seleccionada.getEstacionId() : "EST-01");
        fichaService.actualizarEstado(seleccionada.getId(), Ficha.Estado.LLAMADA);

        // Enviar la ficha directamente al controller padre
        if (controllerPadre != null) {
            controllerPadre.cargarFicha(seleccionada);
        }

        // Cerrar esta ventana
        getStage().close();
    }
}
