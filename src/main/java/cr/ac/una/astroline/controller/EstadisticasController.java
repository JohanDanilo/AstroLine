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

public class EstadisticasController extends Controller implements Initializable {

    private static final DateTimeFormatter FORMATO_EJE = DateTimeFormatter.ofPattern("dd/MM");
    private static final String SIN_DATOS = "Sin datos para el filtro seleccionado.";

    private final EstadisticasService estadisticasService = new EstadisticasService();

    private final Map<String, String> sucursalesPorEtiqueta = new LinkedHashMap<>();

    @FXML
    private Label lblTituloClientes;
    @FXML
    private Label lblTituloTramites;
    @FXML
    private Label lblClientesHoy;
    @FXML
    private Label lblTramitesHoy;

    @FXML
    private LineChart<String, Number> lineChart;
    @FXML
    private PieChart pieChart;

    @FXML
    private ListView<String> lvTopClientes;
    @FXML
    private ListView<String> lvTopTramites;

    @FXML
    private ComboBox<String> cbFiltroTiempo;
    @FXML
    private ComboBox<String> cbSucursal;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    @Override
    public void initialize() {
        setNombreVista("Estadisticas");
        cargarOpcionesFiltros();
        configurarListeners();
        refrescarVista();
    }

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

    private String construirEtiquetaSucursal(SucursalOpcion opcion) {
        if (opcion.getId() == null) {
            return opcion.getNombre();   // "Todas las sucursales"
        }
        String etiquetaBase = opcion.getNombre();
        return sucursalesPorEtiqueta.containsKey(etiquetaBase)
                ? etiquetaBase + " (" + opcion.getId() + ")"
                : etiquetaBase;
    }

    private void configurarListeners() {
        cbFiltroTiempo.setOnAction(e -> {
            if (!ajustandoFiltros) {
                refrescarVista();
            }
        });
        cbSucursal.setOnAction(e -> {
            if (!ajustandoFiltros) {
                refrescarVista();
            }
        });
    }

    private void refrescarVista() {
        String periodoTexto = cbFiltroTiempo.getValue();
        String sucursalId = sucursalesPorEtiqueta.get(cbSucursal.getValue());

        EstadisticasResumen resumen
                = estadisticasService.obtenerResumen(periodoTexto, sucursalId);

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

    private List<String> formatearRanking(List<RankingItem> ranking, String unidad) {
        if (ranking.isEmpty()) {
            return List.of(SIN_DATOS);
        }

        return ranking.stream()
                .map(item -> {
                    long total = item.getTotal();
                    String sufijo = total == 1 ? unidad.replaceAll("s$", "") : unidad;
                    return item.getNombre() + "  —  " + total + " " + sufijo;
                })
                .toList();
    }
}
