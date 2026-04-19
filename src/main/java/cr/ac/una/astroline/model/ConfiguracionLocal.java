package cr.ac.una.astroline.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuración local de la máquina donde corre el sistema.
 * Define en qué sucursal y estación opera este equipo.
 * Se persiste en data/configuracion.json
 * Cada PC del sistema tiene su propio archivo independiente.
 *
 * Adicionalmente mantiene un snapshot de los datos de la sucursal,
 * la estación y los trámiteIds asignados, sincronizado automáticamente
 * cada vez que sucursales.json cambia (P2P o edición local).
 * Esto permite que la estación del funcionario consulte sus trámites
 * sin necesidad de leer sucursales.json en cada operación.
 *
 * @author JohanDanilo
 */
public class ConfiguracionLocal {

    // ── Identidad de la máquina ───────────────────────────────────────────────
    private String sucursalId;
    private String estacionId; // null si esta máquina es un Kiosko o la pantalla de proyección

    // ── Snapshot reactivo (se actualiza automáticamente desde SucursalService) ─
    private List<String> tramiteIds; // IDs de trámites asignados a esta estación

    public ConfiguracionLocal() {
        this.tramiteIds = new ArrayList<>();
    }

    public ConfiguracionLocal(String sucursalId, String estacionId) {
        this.sucursalId = sucursalId;
        this.estacionId = estacionId;
        this.tramiteIds = new ArrayList<>();
    }

    /**
     * Indica si esta máquina está configurada como estación de funcionario.
     * Si estacionId es null, puede operar como Kiosko o como la pantalla de proyección.
     */
    public boolean esEstacionFuncionario() {
        return estacionId != null && !estacionId.isBlank();
    }

    // ── Identidad ─────────────────────────────────────────────────────────────

    public String getSucursalId() { return sucursalId; }
    public void setSucursalId(String sucursalId) { this.sucursalId = sucursalId; }

    public String getEstacionId() { return estacionId; }
    public void setEstacionId(String estacionId) { this.estacionId = estacionId; }

    // ── Snapshot ──────────────────────────────────────────────────────────────

    public List<String> getTramiteIds() {
        return tramiteIds != null ? tramiteIds : new ArrayList<>();
    }
    public void setTramiteIds(List<String> tramiteIds) {
        this.tramiteIds = tramiteIds != null ? tramiteIds : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "ConfiguracionLocal{sucursalId='" + sucursalId +
                "', estacionId='" + estacionId +
                "', tramiteIds=" + tramiteIds + "}";
    }
}