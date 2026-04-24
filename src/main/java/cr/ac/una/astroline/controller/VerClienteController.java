package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Cliente;
import cr.ac.una.astroline.service.ClienteService;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.GsonUtil;
import io.github.palexdev.materialfx.controls.MFXButton;
import java.net.URL;
import java.nio.file.Paths;
import java.io.File;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class VerClienteController extends Controller implements Initializable {

    @FXML
    private MFXButton btnAgregarClientes;
    @FXML
    private MFXButton btnEliminarCliente;
    @FXML
    private MFXButton btnEditarCliente;

    @FXML
    private TableView<Cliente> TbMostrarClientes;
    @FXML
    private TableColumn<Cliente, String> idColumn;
    @FXML
    private TableColumn<Cliente, String> nombreColumn;
    @FXML
    private TableColumn<Cliente, String> apellidoColumn;
    @FXML
    private TableColumn<Cliente, String> telefonoColumn;
    @FXML
    private TableColumn<Cliente, String> correoColumn;
    @FXML
    private TableColumn<Cliente, String> fechaColumn;
    @FXML
    private TableColumn<Cliente, String> fotoColumn;

    private static final String DEFAULT_FOTO_PATH;

    static {
        URL resource = VerClienteController.class.getResource("/cr/ac/una/astroline/resource/LogoUser.png");
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
    public void initialize() {
    }

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

    private Image resolverImagen(String path) {
        try {
            if (path.startsWith("file:") || path.startsWith("jar:")) {
                return new Image(path);
            }
            File archivo = GsonUtil.getDataDir().resolve(path).toAbsolutePath().toFile();
            if (archivo.exists()) {
                return new Image(archivo.toURI().toString());
            }

            archivo = Paths.get(path).toAbsolutePath().toFile();
            if (archivo.exists()) {
                return new Image(archivo.toURI().toString());
            }

        } catch (Exception e) {
            System.err.println("[VerCliente] No se pudo cargar imagen: " + e.getMessage());
        }
        return new Image(DEFAULT_FOTO_PATH);
    }

    private boolean estaEnMainStage() {
        return getStage() == null || getStage() == FlowController.getInstance().getMainStage();
    }

    private void irARegistro(Cliente clienteParaEditar) {
        if (estaEnMainStage()) {
            FlowController.getInstance().goView("RegistroClienteView");
        } else {
            FlowController.getInstance().goViewInCallerStage("RegistroClienteView", this);
        }

        RegistroClienteController ctrl = (RegistroClienteController) FlowController.getInstance().getController("RegistroClienteView");

        if (clienteParaEditar != null) {
            ctrl.cargarClienteParaEditar(clienteParaEditar);
        } else {
            ctrl.limpiarFormulario();
        }
    }

    private void configurarDeseleccion() {
        TbMostrarClientes.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Cliente> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(e -> {
                if (row.isEmpty()) {
                    TbMostrarClientes.getSelectionModel().clearSelection();
                }
            });
            return row;
        });
    }

    @FXML
    private void OnAgregarClientes(ActionEvent event) {
        TbMostrarClientes.getSelectionModel().clearSelection();
        irARegistro(null);
    }

    @FXML
    private void OnEditarCliente(ActionEvent event) {
        Cliente seleccionado = TbMostrarClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Seleccioná un cliente para editar.");
            return;
        }
        irARegistro(seleccionado);
    }

    @FXML
    private void OnEliminarCliente(ActionEvent event) {
        Cliente seleccionado = TbMostrarClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Seleccioná un cliente para eliminar.");
            return;
        }
        clienteService.remover(seleccionado);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
