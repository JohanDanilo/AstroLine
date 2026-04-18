package cr.ac.una.astroline.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.fxml.Initializable;

public class EstadisticasController extends Controller implements Initializable{

    @FXML private Label lblClientesHoy;
    @FXML private Label lblTramitesHoy;
    @FXML private Label lblTiempoPromedio;

    @FXML private LineChart<String, Number> lineChart;
    @FXML private PieChart pieChart;

    @FXML private ListView<String> lvTopClientes;
    @FXML private ListView<String> lvTopTramites;

    @FXML private ComboBox<String> cbFiltroTiempo;
    @FXML private ComboBox<String> cbSucursal;

    @FXML
    public void initialize() {
        cargarFiltros();
        cargarKPIs();
        cargarLineChart();
        cargarPieChart();
        cargarRankings();
    }

    private void cargarFiltros() {
        cbFiltroTiempo.setItems(FXCollections.observableArrayList(
                "Hoy", "Semana", "Mes"
        ));

        cbSucursal.setItems(FXCollections.observableArrayList(
                "Sucursal Centro", "Sucursal Norte", "Sucursal Sur"
        ));
    }

    private void cargarKPIs() {
        lblClientesHoy.setText("120");
        lblTramitesHoy.setText("95");
        lblTiempoPromedio.setText("15 min");
    }

    private void cargarLineChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Trámites");

        series.getData().add(new XYChart.Data<>("Lun", 20));
        series.getData().add(new XYChart.Data<>("Mar", 35));
        series.getData().add(new XYChart.Data<>("Mié", 25));
        series.getData().add(new XYChart.Data<>("Jue", 40));
        series.getData().add(new XYChart.Data<>("Vie", 30));

        lineChart.getData().add(series);
    }

    private void cargarPieChart() {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                new PieChart.Data("Pagos", 40),
                new PieChart.Data("Consultas", 25),
                new PieChart.Data("Reclamos", 20),
                new PieChart.Data("Otros", 15)
        );

        pieChart.setData(data);
    }

    private void cargarRankings() {
        lvTopClientes.setItems(FXCollections.observableArrayList(
                "Juan Pérez - 45 trámites",
                "María López - 40 trámites",
                "Carlos Ruiz - 38 trámites"
        ));

        lvTopTramites.setItems(FXCollections.observableArrayList(
                "Pago de servicios - 60",
                "Consulta general - 50",
                "Reclamos - 30"
        ));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}