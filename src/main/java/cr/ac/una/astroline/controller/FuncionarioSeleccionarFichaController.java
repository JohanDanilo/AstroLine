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

public class FuncionarioSeleccionarFichaController extends Controller implements Initializable {

    @FXML
    private MFXComboBox<Ficha> cmbFichas;
    @FXML
    private MFXButton btnLlamarFichaSeleccionada;

    private final FichaService fichaService = new FichaService();
    private VentanaFuncionarioController controllerPadre;

    @Override
    public void initialize() {
        setNombreVista("Seleccionar Ficha");
        cargarFichasEnEspera();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void setControllerPadre(VentanaFuncionarioController padre) {
        this.controllerPadre = padre;
    }

    private void cargarFichasEnEspera() {
        Respuesta respuesta = fichaService.obtenerFichasActivas();
        if (!respuesta.getEstado()) {
            return;
        }

        ConfiguracionService configuracion = ConfiguracionService.getInstancia();
        configuracion.recargarConfiguracion();
        List<String> tramitesConfigurados = configuracion.getTramitesConfigurados();
        boolean soloPreferencial = configuracion.isPreferencial();

        List<Ficha> activas = (List<Ficha>) respuesta.getResultado("lista");

        List<Ficha> enEspera = activas.stream().filter(Ficha::estaEsperando).filter(f -> tramitesConfigurados.isEmpty() || tramitesConfigurados.contains(f.getTramiteId())).filter(f -> !soloPreferencial || f.isPreferencial()).collect(Collectors.toList());

        cmbFichas.setItems(FXCollections.observableArrayList(enEspera));

        cmbFichas.setConverter(new javafx.util.StringConverter<Ficha>() {
            @Override
            public String toString(Ficha ficha) {
                if (ficha == null) {
                    return "";
                }
                String tipo = ficha.isPreferencial() ? " | Preferencial" : "";
                return ficha.getCodigo() + " | " + ficha.getTramiteId() + tipo;
            }

            @Override
            public Ficha fromString(String string) {
                return null;
            }
        });
    }

    @FXML
    private void OnLlamarFichaSeleccionada(ActionEvent event) {
        Ficha seleccionada = cmbFichas.getValue();

        if (seleccionada == null) {
            return;
        }

        fichaService.registrarLlamado(seleccionada.getId(),
                seleccionada.getEstacionId() != null
                ? seleccionada.getEstacionId() : ConfiguracionService.getInstancia().getEstacionId());

        if (controllerPadre != null) {
            controllerPadre.cargarFicha(seleccionada);
        }

        getStage().close();
    }
}
