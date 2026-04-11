package cr.ac.una.astroline.controller;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import cr.ac.una.astroline.model.Cliente;
import cr.ac.una.astroline.model.ClienteDTO;
import cr.ac.una.astroline.service.ClienteService;
import cr.ac.una.astroline.util.FlowController;

import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.utils.SwingFXUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

public class RegistroClienteController extends Controller implements Initializable {

    // ── FXML fields (sin cambios) ────────────────────────────────────────────
    @FXML private MFXDatePicker dpFechaNacimiento;
    @FXML private MFXTextField  txtCedula;
    @FXML private MFXTextField  txtNombre;
    @FXML private MFXTextField  txtApellido;
    @FXML private MFXTextField  txtTelefono;
    @FXML private MFXTextField  txtCorreo;
    @FXML private AnchorPane    root;
    @FXML private ImageView     fotoCliente;
    @FXML private MFXButton     btnSubirFoto;
    @FXML private MFXButton     btnAbrirCamara;
    @FXML private MFXButton     btnTomarFoto;
    @FXML private MFXButton     btnRegresarAListaClientes;
    @FXML private MFXButton     btnGuardarCambiosClientes;
    private static final String DEFAULT_FOTO_PATH;
    static {
        URL resource = RegistroClienteController.class.getResource(
            "/cr/ac/una/astroline/resource/LogoUser.png");
        DEFAULT_FOTO_PATH = resource != null ? resource.toExternalForm() : "";
    }


    // ── Estado ───────────────────────────────────────────────────────────────
    private final ClienteService clienteService = ClienteService.getInstancia();
    private Cliente editingCliente = null;

    /** Ruta de la foto seleccionada o capturada (puede ser "" si no hay ninguna). */
    private String fotoPathSeleccionado = "";

    /** Carpeta donde se guardan las fotos de clientes. */
    private static final String FOTOS_DIR = "data/fotos/";

    // ── Cámara ───────────────────────────────────────────────────────────────
    private Webcam webcam;
    private ScheduledExecutorService camaraScheduler;
    private final AtomicBoolean camaraActiva = new AtomicBoolean(false);
    @FXML
    private MFXButton btnDescartar;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Deshabilitar "Tomar foto" hasta que la cámara esté abierta
        btnTomarFoto.setDisable(true);
        fotoPathSeleccionado = DEFAULT_FOTO_PATH;
        mostrarImagenLocal(DEFAULT_FOTO_PATH);
    }

    // ── Cargar cliente (modo edición) ─────────────────────────────────────────

    public void cargarClienteParaEditar(Cliente cliente) {
        if (cliente == null) return;
        editingCliente = cliente;

        ClienteDTO dto = new ClienteDTO();
        clienteService.cargarEnDTO(cliente, dto);

        txtCedula.setText(dto.getCedula());
        txtNombre.setText(dto.getNombre());
        txtApellido.setText(dto.getApellidos());
        txtTelefono.setText(dto.getTelefono());
        txtCorreo.setText(dto.getCorreo());
        dpFechaNacimiento.setValue(dto.getFechaNacimiento());
        txtCedula.setEditable(false);

        // Antes: solo cargaba si fotoPath no era vacío
        // Ahora: siempre carga, con fallback al default si está vacío
        String foto = (dto.getFotoPath() != null && !dto.getFotoPath().isEmpty())
                      ? dto.getFotoPath()
                      : DEFAULT_FOTO_PATH;
        fotoPathSeleccionado = foto;
        mostrarImagenLocal(foto);
    }

    // ── Eventos de navegación y guardado (sin cambios) ────────────────────────

    @FXML
    private void OnActionRegresarRegistroCliente(ActionEvent event) {
        detenerCamara();
        FlowController.getInstance().goView("VerClienteView");
    }

    @FXML
    private void OnActionGuardarCambiosClientes(ActionEvent event) {
        if (!camposValidos()) return;
        Cliente cliente = construirClienteDesdeFormulario();

        if (editingCliente != null) {
            actualizarCliente(cliente);
        } else {
            registrarCliente(cliente);
        }
    }

    // ── Foto: subir desde archivo ─────────────────────────────────────────────

    @FXML
    private void onBtnSubirFoto(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar foto del cliente");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File archivo = chooser.showOpenDialog(root.getScene().getWindow());
        if (archivo == null) return; // usuario canceló

        try {
            String destino = copiarFotoADataDir(archivo.toPath(),
                                               generarNombreFoto(archivo.getName()));
            fotoPathSeleccionado = destino;
            mostrarImagenLocal(destino);
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                          "No se pudo copiar la imagen: " + e.getMessage());
        }
    }

    // ── Foto: abrir / cerrar cámara ───────────────────────────────────────────

    @FXML
    private void onBtnAbrirCamara(ActionEvent event) {
        if (camaraActiva.get()) {
            detenerCamara();
        } else {
            iniciarCamara();
        }
    }

    // ── Foto: capturar frame de la cámara ────────────────────────────────────

    @FXML
    private void onBtnTomarFoto(ActionEvent event) {
        if (!camaraActiva.get() || webcam == null) return;

        BufferedImage frame = webcam.getImage();
        if (frame == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Cámara", "No se pudo capturar la imagen.");
            return;
        }

        try {
            String nombre  = "foto_" + System.currentTimeMillis() + ".png";
            Path   destino = Paths.get(FOTOS_DIR, nombre);
            Files.createDirectories(destino.getParent());
            ImageIO.write(frame, "png", destino.toFile());

            fotoPathSeleccionado = destino.toString();
            // Congelar el preview con el frame tomado
            Image fxImage = SwingFXUtils.toFXImage(frame, null);
            Platform.runLater(() -> fotoCliente.setImage(fxImage));

            detenerCamara();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Foto tomada",
                          "La foto se guardó correctamente.");
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                          "No se pudo guardar la foto: " + e.getMessage());
        }
    }

    // ── Lógica de cámara (privado) ───────────────────────────────────────────

    private void iniciarCamara() {
        webcam = Webcam.getDefault();
        if (webcam == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Cámara",
                          "No se detectó ninguna cámara en el sistema.");
            return;
        }

        webcam.setViewSize(WebcamResolution.VGA.getSize());
        webcam.open();
        camaraActiva.set(true);

        btnAbrirCamara.setText("Cerrar cámara");
        btnTomarFoto.setDisable(false);

        // Actualizar el ImageView con cada frame (~15 fps)
        camaraScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "astroline-webcam");
            t.setDaemon(true);
            return t;
        });
        camaraScheduler.scheduleAtFixedRate(() -> {
            if (!camaraActiva.get() || !webcam.isOpen()) return;
            BufferedImage frame = webcam.getImage();
            if (frame == null) return;
            Image fxImage = SwingFXUtils.toFXImage(frame, null);
            Platform.runLater(() -> fotoCliente.setImage(fxImage));
        }, 0, 66, TimeUnit.MILLISECONDS); // ~15 fps
    }

    private void detenerCamara() {
        camaraActiva.set(false);

        if (camaraScheduler != null) {
            camaraScheduler.shutdownNow();
            camaraScheduler = null;
        }
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }

        Platform.runLater(() -> {
            btnAbrirCamara.setText("Abrir cámara");
            btnTomarFoto.setDisable(true);
        });
    }

    // ── Utilidades de imagen ─────────────────────────────────────────────────

    /**
     * Copia una imagen al directorio data/fotos/ y retorna la ruta relativa.
     */
    private String copiarFotoADataDir(Path origen, String nombreDestino) throws IOException {
        Path dirFotos = Paths.get(FOTOS_DIR);
        Files.createDirectories(dirFotos);
        Path destino = dirFotos.resolve(nombreDestino);
        Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);
        return destino.toString();
    }

    /**
     * Genera un nombre único basado en timestamp + extensión original.
     */
    private String generarNombreFoto(String nombreOriginal) {
        String extension = nombreOriginal.contains(".")
            ? nombreOriginal.substring(nombreOriginal.lastIndexOf('.'))
            : ".jpg";
        String cedula = txtCedula.getText().trim();
        return "Cliente_" + cedula + extension;
    }

    /**
     * Muestra una imagen desde una ruta local en el ImageView.
     */
    private void mostrarImagenLocal(String path) {
        if (path == null || path.isEmpty()) return;

        try {
            if (path.startsWith("file:") || path.startsWith("jar:")) {
                // Es una URL de recurso (logo predefinido) — JavaFX la carga directo
                fotoCliente.setImage(new Image(path));
            } else {
                // Es una ruta de archivo en disco (foto subida o capturada)
                File archivo = Paths.get(path).toAbsolutePath().toFile();
                if (archivo.exists()) {
                    fotoCliente.setImage(new Image(archivo.toURI().toString()));
                } else {
                    // Si el archivo ya no existe, mostrar logo por defecto
                    fotoCliente.setImage(new Image(DEFAULT_FOTO_PATH));
                }
            }
        } catch (Exception e) {
            System.err.println("[RegistroCliente] No se pudo cargar imagen: " + e.getMessage());
        }
    }

    // ── Lógica separada (sin cambios relevantes) ──────────────────────────────

    private Cliente construirClienteDesdeFormulario() {
        ClienteDTO dto = new ClienteDTO();
        dto.setCedula(txtCedula.getText().trim());
        dto.setNombre(txtNombre.getText().trim());
        dto.setApellidos(txtApellido.getText().trim());
        dto.setTelefono(txtTelefono.getText().trim());
        dto.setCorreo(txtCorreo.getText().trim());
        dto.setFotoPath(fotoPathSeleccionado.isEmpty() ? DEFAULT_FOTO_PATH : fotoPathSeleccionado);
        dto.setFechaNacimiento(dpFechaNacimiento.getValue());
        return clienteService.dtoACliente(dto);
    }

    private void registrarCliente(Cliente cliente) {
        boolean guardado = clienteService.agregar(cliente);
        if (guardado) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Cliente registrado correctamente.");
            limpiarFormulario();
            navegarALista();
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "Error", "Ya existe un cliente con esa cédula.");
        }
    }

    private void actualizarCliente(Cliente cliente) {
        boolean actualizado = clienteService.actualizar(cliente);
        if (actualizado) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Modificación realizada correctamente.");
            limpiarFormulario();
            navegarALista();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo modificar el cliente.");
        }
    }

    private void navegarALista() {
        detenerCamara();
        FlowController.getInstance().goView("VerClienteView");
    }

    // ── Validaciones (sin cambios) ────────────────────────────────────────────

    private boolean camposValidos() {
        if (txtCedula.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "La cédula es obligatoria.");
            return false;
        }
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "El nombre es obligatorio.");
            return false;
        }
        if (txtApellido.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo requerido", "El apellido es obligatorio.");
            return false;
        }
        if (!esFormatoCorreoValido(txtCorreo.getText().trim())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Correo inválido", "Formato de correo incorrecto.");
            return false;
        }
        return true;
    }

    private boolean esFormatoCorreoValido(String correo) {
        if (correo == null || correo.trim().isEmpty()) return true;
        return correo.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    // ── Formulario ────────────────────────────────────────────────────────────

    public void limpiarFormulario() {
        txtCedula.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        dpFechaNacimiento.setValue(null);
        fotoPathSeleccionado = DEFAULT_FOTO_PATH;
        mostrarImagenLocal(DEFAULT_FOTO_PATH);
        txtCedula.setEditable(true);
        editingCliente = null;
        detenerCamara();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    @Override
    public void initialize() {}



    @FXML
    private void onBtnDescartar(ActionEvent event) {
        if (fotoPathSeleccionado != null
                && !fotoPathSeleccionado.isEmpty()
                && !fotoPathSeleccionado.equals(DEFAULT_FOTO_PATH)) {
            try {
                Path fotoPath = Paths.get(fotoPathSeleccionado).toAbsolutePath();
                Path fotoDir  = Paths.get(FOTOS_DIR).toAbsolutePath();
                if (fotoPath.startsWith(fotoDir)) {
                    Files.deleteIfExists(fotoPath);
                }
            } catch (IOException e) {
                System.err.println("[RegistroCliente] No se pudo borrar la foto: " + e.getMessage());
            }
        }

        // Volver al logo por defecto, no a imagen vacía
        fotoPathSeleccionado = DEFAULT_FOTO_PATH;
        mostrarImagenLocal(DEFAULT_FOTO_PATH);
        detenerCamara();
    }
}