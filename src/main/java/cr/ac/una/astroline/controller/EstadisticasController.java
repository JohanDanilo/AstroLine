package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.service.EstadisticasService;
import cr.ac.una.astroline.service.EstadisticasService.EstadisticasResumen;
import cr.ac.una.astroline.service.EstadisticasService.PeriodoEstadistico;
import cr.ac.una.astroline.service.EstadisticasService.RankingItem;
import cr.ac.una.astroline.service.EstadisticasService.SucursalOpcion;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

/**
 * Controlador de la vista de estadisticas.
 *
 * Ciclo de vida:
 *   initialize(URL, ResourceBundle) → llamado por FXMLLoader durante load().
 *                                     Solo se usa para configuracion de
 *                                     componentes que no dependen de contexto.
 *   initialize()                    → hook de FlowController. Aqui se
 *                                     cargan filtros, se registran listeners
 *                                     y se hace el primer refresco.
 *
 * El servicio es de solo lectura (lee JSON frescos cada vez), por lo que
 * no hay suscripcion a DataNotifier. Si se quiere reactividad en tiempo real
 * se puede llamar a refrescarVista() desde onDataChanged en el futuro.
 *
 * @author JohanDanilo
 */
public class EstadisticasController extends Controller implements Initializable {

    private static final DateTimeFormatter FORMATO_EJE  =
            DateTimeFormatter.ofPattern("dd/MM");
    private static final String            SIN_DATOS    =
            "Sin datos para el filtro seleccionado.";

    // ── Servicio ──────────────────────────────────────────────────────────────

    private final EstadisticasService estadisticasService = new EstadisticasService();

    /**
     * Mapa etiqueta → ID de sucursal para resolver la seleccion del combo.
     * La opcion "Todas las sucursales" mapea a null.
     */
    private final Map<String, String> sucursalesPorEtiqueta = new LinkedHashMap<>();

    // ── FXML ─────────────────────────────────────────────────────────────────

    @FXML private Label lblTituloClientes;
    @FXML private Label lblTituloTramites;
    @FXML private Label lblClientesHoy;
    @FXML private Label lblTramitesHoy;

    @FXML private LineChart<String, Number> lineChart;
    @FXML private PieChart                  pieChart;

    @FXML private ListView<String> lvTopClientes;
    @FXML private ListView<String> lvTopTramites;

    @FXML private ComboBox<String> cbFiltroTiempo;
    @FXML private ComboBox<String> cbSucursal;

    // ── Ciclo de vida ─────────────────────────────────────────────────────────

    /**
     * Llamado por FXMLLoader durante load().
     * No hay logica aqui — todo va en initialize() para respetar el ciclo
     * de vida de FlowController.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Sin logica de datos — ver initialize() abajo
    }

    /**
     * Hook de FlowController. Corre despues de que el stage esta listo.
     * 1. Carga las opciones de los combos.
     * 2. Registra los listeners (aqui, no antes, para evitar disparos prematuros).
     * 3. Hace el primer refresco de la vista.
     */
    @Override
    public void initialize() {
        setNombreVista("Estadisticas");
        cargarOpcionesFiltros();
        configurarListeners();
        refrescarVista();
    }

    // ── Filtros ───────────────────────────────────────────────────────────────

    /**
     * Poblacion de los combos de filtro.
     * Se usa una bandera ajustandoFiltros para que la poblacion inicial
     * no dispare onAction y provoque un refresco prematuro.
     */
    private boolean ajustandoFiltros = false;

    private void cargarOpcionesFiltros() {
        ajustandoFiltros = true;
        try {
            cargarComboPeriodo();
            cargarComboSucursal();
        } finally {
            ajustandoFiltros = false;
        }
    }

    private void cargarComboPeriodo() {
        // Preservar la seleccion actual si ya habia una
        String periodoActual = cbFiltroTiempo.getValue();

        cbFiltroTiempo.setItems(FXCollections.observableArrayList(
                PeriodoEstadistico.HOY.getEtiquetaFiltro(),
                PeriodoEstadistico.SEMANA.getEtiquetaFiltro(),
                PeriodoEstadistico.MES.getEtiquetaFiltro()
        ));

        if (periodoActual != null && cbFiltroTiempo.getItems().contains(periodoActual)) {
            cbFiltroTiempo.setValue(periodoActual);
        } else {
            cbFiltroTiempo.getSelectionModel().selectFirst();
        }
    }

    private void cargarComboSucursal() {
        // Preservar la seleccion actual si ya habia una
        String sucursalActual = cbSucursal.getValue();
        sucursalesPorEtiqueta.clear();

        ObservableList<String> opciones = FXCollections.observableArrayList();
        for (SucursalOpcion opcion : estadisticasService.obtenerSucursalesDisponibles()) {
            String etiqueta = construirEtiquetaSucursal(opcion);
            sucursalesPorEtiqueta.put(etiqueta, opcion.getId());
            opciones.add(etiqueta);
        }
        cbSucursal.setItems(opciones);

        if (sucursalActual != null && sucursalesPorEtiqueta.containsKey(sucursalActual)) {
            cbSucursal.setValue(sucursalActual);
        } else if (!opciones.isEmpty()) {
            cbSucursal.getSelectionModel().selectFirst();
        }
    }

    /**
     * Construye la etiqueta visible del combo de sucursales.
     * Si hay conflicto de nombre se agrega el ID entre parentesis.
     */
    private String construirEtiquetaSucursal(SucursalOpcion opcion) {
        if (opcion.getId() == null) return opcion.getNombre();   // "Todas las sucursales"
        String etiquetaBase = opcion.getNombre();
        return sucursalesPorEtiqueta.containsKey(etiquetaBase)
                ? etiquetaBase + " (" + opcion.getId() + ")"
                : etiquetaBase;
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    /**
     * Registra los listeners de los combos.
     * Los listeners solo disparan refrescarVista() si no estamos en medio
     * de la carga inicial de filtros (ajustandoFiltros == false).
     *
     * No usa bandera listenersConfigurados: initialize() de FlowController
     * se llama una sola vez por instancia de controlador.
     */
    private void configurarListeners() {
        cbFiltroTiempo.setOnAction(e -> { if (!ajustandoFiltros) refrescarVista(); });
        cbSucursal.setOnAction(    e -> { if (!ajustandoFiltros) refrescarVista(); });
    }

    // ── Refresco de vista ─────────────────────────────────────────────────────

    /**
     * Punto de entrada para refrescar todos los widgets con datos frescos.
     * Guarda contra combos vacios — si el valor es null se usan defaults.
     */
    private void refrescarVista() {
        String periodoTexto = cbFiltroTiempo.getValue();
        String sucursalId   = sucursalesPorEtiqueta.get(cbSucursal.getValue());

        EstadisticasResumen resumen =
                estadisticasService.obtenerResumen(periodoTexto, sucursalId);

        actualizarTitulos(resumen.getPeriodo());
        cargarKPIs(resumen);
        cargarLineChart(resumen);
        cargarPieChart(resumen);
        cargarRankings(resumen);
    }

    private void actualizarTitulos(PeriodoEstadistico periodo) {
        lblTituloClientes.setText(periodo.getTituloClientes());
        lblTituloTramites.setText(periodo.getTituloTramites());
    }

    private void cargarKPIs(EstadisticasResumen resumen) {
        lblClientesHoy.setText(String.valueOf(resumen.getTotalClientes()));
        lblTramitesHoy.setText(String.valueOf(resumen.getTotalTramites()));
    }

    private void cargarLineChart(EstadisticasResumen resumen) {
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Tramites");

        for (Map.Entry<LocalDate, Long> entry : resumen.getTramitesPorDia().entrySet()) {
            serie.getData().add(new XYChart.Data<>(
                    entry.getKey().format(FORMATO_EJE),
                    entry.getValue()
            ));
        }

        lineChart.getData().setAll(serie);
    }

    private void cargarPieChart(EstadisticasResumen resumen) {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        for (Map.Entry<String, Long> entry : resumen.getDistribucionTramites().entrySet()) {
            data.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        pieChart.setData(data);
    }

    private void cargarRankings(EstadisticasResumen resumen) {
        lvTopClientes.setItems(FXCollections.observableArrayList(
                formatearRanking(resumen.getTopClientes(), "visitas")));
        lvTopTramites.setItems(FXCollections.observableArrayList(
                formatearRanking(resumen.getTopTramites(), "solicitudes")));
    }

    /**
     * Formatea una lista de RankingItem como strings para el ListView.
     *
     * @param ranking  lista de items a formatear
     * @param unidad   etiqueta de la unidad ("visitas", "solicitudes", etc.)
     */
    private List<String> formatearRanking(List<RankingItem> ranking, String unidad) {
        if (ranking.isEmpty()) return List.of(SIN_DATOS);

        return ranking.stream()
                .map(item -> {
                    long total = item.getTotal();
                    String sufijo = total == 1 ? unidad.replaceAll("s$", "") : unidad;
                    return item.getNombre() + "  —  " + total + " " + sufijo;
                })
                .toList();
    }
}