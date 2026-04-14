package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Cliente;
import cr.ac.una.astroline.service.ClienteService;
import cr.ac.una.astroline.util.FlowController;
import io.github.palexdev.materialfx.controls.MFXButton;
import java.net.URL;
import java.nio.file.Paths;
import java.io.File;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class VerClienteController extends Controller implements Initializable {

    @FXML private MFXButton btnAgregarClientes;
    @FXML private MFXButton btnEliminarCliente;
    @FXML private MFXButton btnEditarCliente;

    @FXML private TableView<Cliente>           TbMostrarClientes;
    @FXML private TableColumn<Cliente, String> idColumn;
    @FXML private TableColumn<Cliente, String> nombreColumn;
    @FXML private TableColumn<Cliente, String> apellidoColumn;
    @FXML private TableColumn<Cliente, String> telefonoColumn;
    @FXML private TableColumn<Cliente, String> correoColumn;
    @FXML private TableColumn<Cliente, String> fechaColumn;
    @FXML private TableColumn<Cliente, String> fotoColumn; // ← nueva columna en el FXML

    private static final String DEFAULT_FOTO_PATH;
    static {
        URL resource = VerClienteController.class.getResource(
            "/cr/ac/una/astroline/resource/LogoUser.png");
        DEFAULT_FOTO_PATH = resource != null ? resource.toExternalForm() : "";
    }

    private final ClienteService clienteService = ClienteService.getInstancia();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (TbMostrarClientes != null) {
            TbMostrarClientes.refresh();
        }
        configurarColumnas();
        cargarTabla();
    }

    @Override
    public void initialize() {}

    // ── Configuración de tabla ────────────────────────────────────────────────

    private void configurarColumnas() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("cedula"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        apellidoColumn.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        telefonoColumn.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        correoColumn.setCellValueFactory(new PropertyValueFactory<>("correo"));
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaNacimiento"));

        configurarColumnaFoto();
        configurarDeseleccion();
    }

    private void configurarColumnaFoto() {
        fotoColumn.setCellValueFactory(new PropertyValueFactory<>("fotoPath"));
        fotoColumn.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
            }

            @Override
            protected void updateItem(String fotoPath, boolean empty) {
                super.updateItem(fotoPath, empty);
                if (empty || fotoPath == null || fotoPath.isEmpty()) {
                    setGraphic(null);
                    return;
                }
                imageView.setImage(resolverImagen(fotoPath));
                setAlignment(Pos.CENTER);
                setGraphic(imageView);
            }
        });
    }

    private void cargarTabla() {
        TbMostrarClientes.setItems(clienteService.getListaDeClientes());
    }

    // ── Resolver ruta → Image (mismo criterio que RegistroClienteController) ──

    private Image resolverImagen(String path) {
        try {
            if (path.startsWith("file:") || path.startsWith("jar:")) {
                return new Image(path);
            }
            File archivo = Paths.get(path).toAbsolutePath().toFile();
            if (archivo.exists()) {
                return new Image(archivo.toURI().toString());
            }
        } catch (Exception e) {
            System.err.println("[VerCliente] No se pudo cargar imagen: " + e.getMessage());
        }
        // Fallback al logo si la foto no se encuentra
        return new Image(DEFAULT_FOTO_PATH);
    }

    // ── Eventos ───────────────────────────────────────────────────────────────

    private void configurarDeseleccion() {
        TbMostrarClientes.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Cliente> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(e -> {
                // Si el click fue en una fila vacía, limpiar selección
                if (row.isEmpty()) {
                    TbMostrarClientes.getSelectionModel().clearSelection();
                }
            });
            return row;
        });
    }

    @FXML
    private void OnAgregarClientes(ActionEvent event) {
        FlowController.getInstance().goView("RegistroClienteView");

        // Limpiar el controlador cacheado para que no arrastre datos del cliente anterior
        RegistroClienteController controller = (RegistroClienteController)
            FlowController.getInstance().getController("RegistroClienteView");
        controller.limpiarFormulario();

        // Limpiar la selección de la tabla para que no quede ninguna fila resaltada
        TbMostrarClientes.getSelectionModel().clearSelection();
    }

    @FXML
    private void OnEditarCliente(ActionEvent event) {
        Cliente clienteSeleccionado = TbMostrarClientes.getSelectionModel().getSelectedItem();
        if (clienteSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección",
                          "Seleccioná un cliente para editar.");
            return;
        }
        FlowController.getInstance().goView("RegistroClienteView");
        RegistroClienteController controller = (RegistroClienteController)
            FlowController.getInstance().getController("RegistroClienteView");
        controller.cargarClienteParaEditar(clienteSeleccionado);
    }

    @FXML
    private void OnEliminarCliente(ActionEvent event) {
        Cliente clienteSeleccionado = TbMostrarClientes.getSelectionModel().getSelectedItem();
        if (clienteSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección",
                          "Seleccioná un cliente para eliminar.");
            return;
        }
        clienteService.remover(clienteSeleccionado);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}