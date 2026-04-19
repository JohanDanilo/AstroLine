package cr.ac.una.astroline.service;

import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.model.EmpresaDTO;
import cr.ac.una.astroline.util.DataNotifier;
import cr.ac.una.astroline.util.GsonUtil;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Servicio singleton para la gestión persistente de la empresa.
 * Empresa es un objeto único (no lista), por lo que no usa tombstones.
 * Reactivo y sincronizado entre peers via DataNotifier.
 *
 * @author JohanDanilo
 */
public class EmpresaService implements DataNotifier.Listener {

    private Empresa empresa;

    /**
     * Property observable para que la UI reaccione a cambios P2P sin polling.
     * Los controladores pueden hacer: empresaService.getEmpresaProperty().addListener(...)
     */
    private final ObjectProperty<Empresa> empresaProperty = new SimpleObjectProperty<>();

    private static EmpresaService instancia;

    private static final String ARCHIVO_JSON = "empresa.json";

    private EmpresaService() {
        cargarEmpresa();
        DataNotifier.subscribe(this);
    }

    public static EmpresaService getInstancia() {
        if (instancia == null) {
            instancia = new EmpresaService();
        }
        return instancia;
    }

    // ── Acceso al modelo ─────────────────────────────────────────────────────

    public Empresa getEmpresa() {
        return empresa;
    }

    public ObjectProperty<Empresa> getEmpresaProperty() {
        return empresaProperty;
    }

    // ── Conversiones DTO ↔ Modelo ────────────────────────────────────────────

    /**
     * Carga los datos de la empresa en un DTO para binding con el formulario.
     * Incluye pinAdmin para que el admin pueda verlo y modificarlo.
     */
    public void cargarEnDTO(Empresa empresa, EmpresaDTO dto) {
        if (empresa == null || dto == null) return;

        dto.setNombre(empresa.getNombre());
        dto.setTelefono(empresa.getTelefono());
        dto.setCorreo(empresa.getCorreo());
        dto.setLogoPath(empresa.getLogoPath());
        dto.setDireccion(empresa.getDireccion());
        dto.setPinAdmin(empresa.getPinAdmin());
    }

    /**
     * Convierte un DTO de empresa a un objeto Empresa listo para persistir.
     */
    public Empresa dtoAEmpresa(EmpresaDTO dto) {
        if (dto == null) return null;

        return new Empresa(
                dto.getNombre(),
                dto.getLogoPath(),
                dto.getPinAdmin(),
                dto.getTelefono(),
                dto.getCorreo(),
                dto.getDireccion()
        );
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    /**
     * Actualiza los datos de la empresa y propaga a todos los peers en red.
     * Es el único método de escritura — empresa no se elimina ni se crea
     * en tiempo de ejecución (DataInitializer la crea si no existe).
     *
     * @param empresaActualizada empresa con los nuevos datos
     * @return true si se guardó correctamente
     */
    public boolean actualizar(Empresa empresaActualizada) {
        if (empresaActualizada == null) return false;

        empresaActualizada.setLastModified(System.currentTimeMillis());
        this.empresa = empresaActualizada;
        empresaProperty.set(this.empresa);
        GsonUtil.guardarYPropagar(this.empresa, ARCHIVO_JSON);
        return true;
    }

    // ── Carga inicial ────────────────────────────────────────────────────────

    private void cargarEmpresa() {
        Empresa cargada = GsonUtil.leer(ARCHIVO_JSON, Empresa.class);
        // DataInitializer garantiza que empresa.json siempre existe al arrancar,
        // pero se deja el fallback por si se corre en modo standalone sin inicializar.
        this.empresa = cargada != null ? cargada : new Empresa();
        empresaProperty.set(this.empresa);
    }

    // ── Reactividad (cambios externos desde peers) ───────────────────────────

    /**
     * Llamado por DataNotifier cuando SyncServer recibe empresa.json de un peer.
     * Se ejecuta desde el hilo HTTP — plataforma JavaFX requiere runLater.
     */
    @Override
    public void onDataChanged(String fileName) {
        if (!ARCHIVO_JSON.equals(fileName)) return;

        System.out.println("[EmpresaService] Detectado cambio externo, sincronizando...");

        Platform.runLater(() -> {
            Empresa remota = GsonUtil.leer(ARCHIVO_JSON, Empresa.class);
            if (remota != null) mergeEmpresa(remota);
        });
    }

    /**
     * Merge para objeto único: gana el registro con mayor lastModified.
     * No hay tombstones porque la empresa nunca se elimina.
     *
     * Si la empresa local nunca fue modificada (lastModified == 0),
     * la remota siempre gana para absorber la configuración inicial
     * del equipo administrador.
     */
    private void mergeEmpresa(Empresa remota) {
        if (empresa == null || remota.getLastModified() >= empresa.getLastModified()) {
            this.empresa = remota;
            empresaProperty.set(this.empresa);
            System.out.println("[EmpresaService] Empresa actualizada desde peer.");
        }
    }
}