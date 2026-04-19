package cr.ac.una.astroline.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuración local de la máquina donde corre el sistema.
 * Define en qué sucursal y estación opera este equipo.
 * Se persiste en data/configuracion.json
 * Cada PC del sistema tiene su propio archivo independiente.
 *
 * @author JohanDanilo
 */
public class ConfiguracionLocal {

    private String sucursalId;
    private String estacionId; // null si esta máquina es un Kiosko o la pantalla de proyeccion
    private List<String> tramiteIds;
    private boolean preferencial;

    public ConfiguracionLocal() {
        this.tramiteIds = new ArrayList<>();
    }

    public ConfiguracionLocal(String sucursalId, String estacionId, List<String> tramiteIds, boolean preferencial) {
        this.sucursalId  = sucursalId;
        this.estacionId  = estacionId;
        this.tramiteIds  = tramiteIds != null ? tramiteIds : new ArrayList<>();
        this.preferencial = preferencial;
    }

    /**
     * Indica si esta máquina está configurada como estación de funcionario.
     * Si estacionId es null, puede operar como Kiosko o como la panatalla de proyeccion.
     */
    public boolean esEstacionFuncionario() {
        return estacionId != null && !estacionId.isBlank();
    }

    public String getSucursalId() { return sucursalId; }
    public void setSucursalId(String sucursalId) { this.sucursalId = sucursalId; }

    public String getEstacionId() { return estacionId; }
    public void setEstacionId(String estacionId) { this.estacionId = estacionId; }
    
    public List<String> getTramiteIds() {
        return tramiteIds;
    }

    public void setTramiteIds(List<String> tramiteIds) {
        this.tramiteIds = tramiteIds != null ? tramiteIds : new ArrayList<>();
    }

    public boolean isPreferencial() { return preferencial; }
    public void setPreferencial(boolean preferencial) { this.preferencial = preferencial; }

    @Override
    public String toString() {
        return "ConfiguracionLocal{sucursalId='" + sucursalId +
                "', estacionId='" + estacionId + "'}";
    }
}