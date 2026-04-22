package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.model.EmpresaDTO;
import cr.ac.una.astroline.model.Funcionario;
import cr.ac.una.astroline.model.FuncionarioDTO;
import cr.ac.una.astroline.service.EmpresaService;
import cr.ac.una.astroline.service.FuncionarioService;
import cr.ac.una.astroline.util.GsonUtil;
import cr.ac.una.astroline.util.PathManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class MantenimientoParametrosGeneralesController extends Controller implements Initializable {

    @FXML
    private AnchorPane root;
    @FXML
    private ImageView imgLogoEmpresa;
    @FXML
    private MFXButton btnSeleccionarLogo;
    @FXML
    private MFXButton btnDescartarLogo;
    @FXML
    private MFXTextField txtNombreEmpresa;
    @FXML
    private MFXTextField txtTelefonoEmpresa;
    @FXML
    private MFXTextField txtCorreoEmpresa;
    @FXML
    private MFXTextField txtDireccionEmpresa;
    @FXML
    private MFXPasswordField txtPinAdminEmpresa;
    @FXML
    private MFXButton btnGuardarEmpresa;
    @FXML
    private MFXTextField txtBusquedaFuncionario;
    @FXML
    private MFXButton btnNuevoFuncionario;
    @FXML
    private TableView<Funcionario> tblFuncionarios;
    @FXML
    private TableColumn<Funcionario, String> colCedula;
    @FXML
    private TableColumn<Funcionario, String> colNombreFuncionario;
    @FXML
    private TableColumn<Funcionario, String> colUsername;
    @FXML
    private TableColumn<Funcionario, String> colRol;
    @FXML
    private MFXTextField txtCedulaFuncionario;
    @FXML
    private MFXTextField txtNombreFuncionario;
    @FXML
    private MFXTextField txtApellidosFuncionario;
    @FXML
    private MFXTextField txtUsernameFuncionario;
    @FXML
    private MFXPasswordField txtPasswordFuncionario;
    @FXML
    private MFXCheckbox chkEsAdmin;
    @FXML
    private MFXDatePicker dpFechaNacimiento;
    @FXML
    private MFXButton btnGuardarFuncionario;
    @FXML
    private MFXButton btnEliminarFuncionario;

// Administradores 
    @FXML
    private TableView<Funcionario> tblAdmins;
    @FXML
    private TableColumn<Funcionario, String> colAdminNombre;
    @FXML
    private TableColumn<Funcionario, String> colAdminUsername;
    @FXML
    private TableColumn<Funcionario, String> colAdminCedula;

    private final FuncionarioService funcionarioService = FuncionarioService.getInstancia();
    private final EmpresaService empresaService = EmpresaService.getInstancia();
    private Funcionario editingFuncionario = null;

    private FilteredList<Funcionario> filteredFuncionarios;

    private static final String DEFAULT_LOGO_PATH;

    static {
        URL resource = MantenimientoParametrosGeneralesController.class.getResource(
                "/cr/ac/una/astroline/resource/LogoEmpresa.png");
        DEFAULT_LOGO_PATH = resource != null ? resource.toExternalForm() : "";
    }

    private String logoPathSeleccionado = "";
    private static final String LOGO_SUBDIR = "logoEmpresa";
    @FXML
    private MFXTextField txtRutaDatos;
    @FXML
    private Button btnExaminarRuta;
    @FXML
    private Button btnGuardarRuta;

    @Override
    public void initialize() {
        setNombreVista("Parámetros Generales");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTablaFuncionarios();
        cargarTablaFuncionarios();
        configurarBusquedaFuncionario();
        configurarSeleccionTabla();
        configurarTablaAdmins();
        cargarTablaAdmins();
        cargarDatosEmpresa();
        cargarRutaActual();
        empresaService.getEmpresaProperty().addListener(
                (ObservableValue<? extends Empresa> obs, Empresa vieja, Empresa nueva) -> {
                    if (nueva != null) {
                        poblarFormularioEmpresa(nueva);
                    }
                }
        );

    }

    //Empresa
    private void cargarDatosEmpresa() {
        Empresa empresa = empresaService.getEmpresa();
        if (empresa == null) {
            return;
        }
        poblarFormularioEmpresa(empresa);
    }

    private void poblarFormularioEmpresa(Empresa empresa) {
        EmpresaDTO dto = new EmpresaDTO();
        empresaService.cargarEnDTO(empresa, dto);

        txtNombreEmpresa.setText(dto.getNombre());
        txtTelefonoEmpresa.setText(dto.getTelefono());
        txtCorreoEmpresa.setText(dto.getCorreo());
        txtDireccionEmpresa.setText(dto.getDireccion());
        txtPinAdminEmpresa.setText(dto.getPinAdmin());

        logoPathSeleccionado = dto.getLogoPath() != null ? dto.getLogoPath() : "";
        mostrarImagenLocal(logoPathSeleccionado.isEmpty() ? DEFAULT_LOGO_PATH : logoPathSeleccionado);
    }

    @FXML
    private void onBtnGuardarEmpresa(ActionEvent event) {
        if (!camposValidosEmpresa()) {
            return;
        }

        Empresa empresa = construirEmpresaDesdeFormulario();
        boolean guardado = empresaService.actualizar(empresa);

        if (guardado) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Parámetros de empresa guardados.");
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudieron guardar los parámetros.");
        }
    }

    private Empresa construirEmpresaDesdeFormulario() {
        EmpresaDTO dto = new EmpresaDTO();
        dto.setNombre(txtNombreEmpresa.getText().trim());
        dto.setTelefono(txtTelefonoEmpresa.getText().trim());
        dto.setCorreo(txtCorreoEmpresa.getText().trim());
        dto.setDireccion(txtDireccionEmpresa.getText().trim());
        dto.setPinAdmin(txtPinAdminEmpresa.getText().trim());
        dto.setLogoPath(logoPathSeleccionado);
        return empresaService.dtoAEmpresa(dto);
    }

    // ── Logo
    @FXML
    private void onBtnSeleccionarLogo(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar logo de la empresa");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File archivo = chooser.showOpenDialog(root.getScene().getWindow());
        if (archivo == null) {
            return;
        }

        try {
            String ext = archivo.getName().contains(".") ? archivo.getName().substring(archivo.getName().lastIndexOf('.')) : ".png";
            String nombreDestino = "logo_empresa" + ext;
            String relPath = copiarImagenADataDir(archivo.toPath(), nombreDestino);
            logoPathSeleccionado = relPath;
            mostrarImagenLocal(relPath);
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo copiar la imagen: " + e.getMessage());
        }
    }

    @FXML
    private void onBtnDescartarLogo(ActionEvent event) {
        if (logoPathSeleccionado != null
                && !logoPathSeleccionado.isEmpty()
                && !logoPathSeleccionado.equals(DEFAULT_LOGO_PATH)
                && !logoPathSeleccionado.startsWith("file:")
                && !logoPathSeleccionado.startsWith("jar:")) {
            try {
                Path logoPath = Paths.get(GsonUtil.getDataDir(),
                        logoPathSeleccionado.split("/")).toAbsolutePath();
                Path logoDir = Paths.get(GsonUtil.getDataDir(),
                        LOGO_SUBDIR).toAbsolutePath();
                if (logoPath.startsWith(logoDir)) {
                    Files.deleteIfExists(logoPath);
                }
            } catch (IOException e) {
                System.err.println("[MantenimientoParametros] No se pudo borrar el logo: " + e.getMessage());
            }
        }
        logoPathSeleccionado = DEFAULT_LOGO_PATH;
        mostrarImagenLocal(DEFAULT_LOGO_PATH);
    }

    private void configurarTablaFuncionarios() {
        colCedula.setCellValueFactory(new PropertyValueFactory<>("cedula"));
        colNombreFuncionario.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRol.setCellValueFactory(data
                -> new SimpleStringProperty(data.getValue().esAdmin() ? "Admin" : "Funcionario")
        );
    }

    private void cargarTablaFuncionarios() {
        filteredFuncionarios = new FilteredList<>(
                funcionarioService.getListaDeFuncionarios(),
                f -> true
        );

        SortedList<Funcionario> sortedFuncionarios = new SortedList<>(filteredFuncionarios);
        sortedFuncionarios.comparatorProperty().bind(tblFuncionarios.comparatorProperty());

        tblFuncionarios.setItems(sortedFuncionarios);
    }

    private void configurarBusquedaFuncionario() {
        txtBusquedaFuncionario.textProperty().addListener((obs, oldVal, newVal) -> {
            String filtro = (newVal == null) ? "" : newVal.trim().toLowerCase();

            if (filtro.isEmpty()) {
                filteredFuncionarios.setPredicate(f -> true);
                return;
            }

            filteredFuncionarios.setPredicate(f -> {
                if (f.getCedula() != null && f.getCedula().toLowerCase().contains(filtro)) {
                    return true;
                }
                if (f.getNombre() != null && f.getNombre().toLowerCase().contains(filtro)) {
                    return true;
                }
                if (f.getApellidos() != null && f.getApellidos().toLowerCase().contains(filtro)) {
                    return true;
                }
                if (f.getUsername() != null && f.getUsername().toLowerCase().contains(filtro)) {
                    return true;
                }
                return false;
            });
        });
    }

    private void configurarSeleccionTabla() {
        tblFuncionarios.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionado) -> {
                    if (seleccionado != null) {
                        cargarFuncionarioParaEditar(seleccionado);
                    }
                }
        );
    }

    private void configurarTablaAdmins() {
        colAdminNombre.setCellValueFactory(data
                -> new SimpleStringProperty(
                        data.getValue().getNombre() + " " + data.getValue().getApellidos()
                )
        );
        colAdminUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colAdminCedula.setCellValueFactory(new PropertyValueFactory<>("cedula"));
    }

    private void cargarTablaAdmins() {
        FilteredList<Funcionario> soloAdmins = new FilteredList<>(
                funcionarioService.getListaDeFuncionarios(),
                Funcionario::esAdmin
        );
        tblAdmins.setItems(soloAdmins);
    }

    @FXML
    private void onBtnNuevoFuncionario(ActionEvent event) {
        limpiarFormularioFuncionario();
    }

    @FXML
    private void onBtnGuardarFuncionario(ActionEvent event) {
        if (!camposValidosFuncionario()) {
            return;
        }
        Funcionario funcionario = construirFuncionarioDesdeFormulario();

        if (editingFuncionario != null) {
            actualizarFuncionario(funcionario);
        } else {
            registrarFuncionario(funcionario);
        }
    }

    @FXML
    private void onBtnEliminarFuncionario(ActionEvent event) {
        Funcionario seleccionado = tblFuncionarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección", "Seleccioná un funcionario para eliminar.");
            return;
        }
        boolean eliminado = funcionarioService.remover(seleccionado);
        if (eliminado) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Funcionario eliminado.");
            limpiarFormularioFuncionario();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el funcionario.");
        }
    }

    private void cargarFuncionarioParaEditar(Funcionario funcionario) {
        if (funcionario == null) {
            return;
        }
        editingFuncionario = funcionario;

        FuncionarioDTO dto = new FuncionarioDTO();
        funcionarioService.cargarEnDTO(funcionario, dto);

        txtCedulaFuncionario.setText(dto.getCedula());
        txtNombreFuncionario.setText(dto.getNombre());
        txtApellidosFuncionario.setText(dto.getApellidos());
        txtUsernameFuncionario.setText(dto.getUsername());
        txtPasswordFuncionario.setText(dto.getPassword());
        dpFechaNacimiento.setValue(dto.getFechaNacimiento());
        chkEsAdmin.setSelected(dto.esAdmin());
        txtCedulaFuncionario.setEditable(false);
    }

    private Funcionario construirFuncionarioDesdeFormulario() {
        FuncionarioDTO dto = new FuncionarioDTO();
        dto.setCedula(txtCedulaFuncionario.getText().trim());
        dto.setNombre(txtNombreFuncionario.getText().trim());
        dto.setApellidos(txtApellidosFuncionario.getText().trim());
        dto.setUsername(txtUsernameFuncionario.getText().trim());
        dto.setPassword(txtPasswordFuncionario.getText().trim());
        dto.setFechaNacimiento(dpFechaNacimiento.getValue());
        dto.setEsAdmin(chkEsAdmin.isSelected());
        return funcionarioService.dtoAFuncionario(dto);
    }

    private void registrarFuncionario(Funcionario funcionario) {
        boolean guardado = funcionarioService.agregar(funcionario);
        if (guardado) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Funcionario registrado correctamente.");
            limpiarFormularioFuncionario();
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "Duplicado", "Ya existe un funcionario con esa cédula.");
        }
    }

    private void actualizarFuncionario(Funcionario funcionario) {
        boolean actualizado = funcionarioService.actualizar(funcionario);
        if (actualizado) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Funcionario modificado correctamente.");
            limpiarFormularioFuncionario();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo modificar el funcionario.");
        }
    }

    private boolean camposValidosEmpresa() {
        if (txtNombreEmpresa.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "El nombre de la empresa es obligatorio.");
            return false;
        }
        if (txtTelefonoEmpresa.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "El teléfono es obligatorio.");
            return false;
        }
        if (txtCorreoEmpresa.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "El correo es obligatorio.");
            return false;
        }
        if (txtDireccionEmpresa.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "La dirección es obligatoria.");
            return false;
        }
        if (txtPinAdminEmpresa.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "El PIN de administrador es obligatorio.");
            return false;
        }
        return true;
    }

    private boolean camposValidosFuncionario() {
        if (txtCedulaFuncionario.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "La cédula es obligatoria.");
            return false;
        }
        if (txtNombreFuncionario.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "El nombre es obligatorio.");
            return false;
        }
        if (txtApellidosFuncionario.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "Los apellidos son obligatorios.");
            return false;
        }
        if (txtUsernameFuncionario.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "El username es obligatorio.");
            return false;
        }
        if (txtPasswordFuncionario.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "La contraseña es obligatoria.");
            return false;
        }
        return true;
    }

    public void limpiarFormularioFuncionario() {
        txtCedulaFuncionario.clear();
        txtNombreFuncionario.clear();
        txtApellidosFuncionario.clear();
        txtUsernameFuncionario.clear();
        txtPasswordFuncionario.clear();
        dpFechaNacimiento.setValue(null);
        chkEsAdmin.setSelected(false);
        txtCedulaFuncionario.setEditable(true);
        editingFuncionario = null;
        tblFuncionarios.getSelectionModel().clearSelection();
    }

    private String copiarImagenADataDir(Path origen, String nombreDestino) throws IOException {
        Path dir = Paths.get(GsonUtil.getDataDir(), LOGO_SUBDIR);
        Files.createDirectories(dir);
        Path destino = dir.resolve(nombreDestino);
        Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);
        return LOGO_SUBDIR + "/" + nombreDestino;
    }

    private void mostrarImagenLocal(String path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        try {
            if (path.startsWith("file:") || path.startsWith("jar:")) {
                imgLogoEmpresa.setImage(new Image(path));
                return;
            }
            File archivo = Paths.get(GsonUtil.getDataDir(),
                    path.split("/")).toAbsolutePath().toFile();
            if (archivo.exists()) {
                imgLogoEmpresa.setImage(new Image(archivo.toURI().toString()));
                return;
            }
            archivo = Paths.get(path).toAbsolutePath().toFile();
            imgLogoEmpresa.setImage(archivo.exists()
                    ? new Image(archivo.toURI().toString())
                    : new Image(DEFAULT_LOGO_PATH));
        } catch (Exception e) {
            System.err.println("[MantenimientoParametros] No se pudo cargar imagen: " + e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void cargarRutaActual(){
        txtRutaDatos.setText(PathManager.getDataPath().toString());
    }

    @FXML
    private void onBtnExaminarRuta(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Seleccionar carpeta de datos de AstroLine");

        File actual = PathManager.getDataPath().toFile();
        if (actual.exists()) {
            chooser.setInitialDirectory(actual);
        }

        File seleccionada = chooser.showDialog(txtRutaDatos.getScene().getWindow());
        if (seleccionada != null) {
            txtRutaDatos.setText(seleccionada.getAbsolutePath());
        }
    }

    @FXML
    private void onBtnGuardarRuta(ActionEvent event){
        String texto = txtRutaDatos.getText();
        if (texto == null || texto.isBlank()) {
            mostrarAlerta("Campo vacío", "Debes ingresar o seleccionar una ruta válida.");
            return;
        }

        Path nueva = Path.of(texto.trim());

        try {
            Files.createDirectories(nueva);
            PathManager.setDataPath(nueva);
            mostrarAlerta("Listo", 
                "Ruta guardada correctamente.\n\nReinicia la aplicación para que todas las instancias usen la nueva ubicación.");
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo guardar la ruta: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
