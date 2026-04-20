package cr.ac.una.astroline.model;

import java.util.ArrayList;
import java.util.List;

public class ConfiguracionLocal {

    private String sucursalId;

    private String estacionId;
    private List<String> tramiteIds;
    private boolean preferencial;

    public ConfiguracionLocal() {
        this.tramiteIds = new ArrayList<>();
    }

    public ConfiguracionLocal(String sucursalId, String estacionId, List<String> tramiteIds, boolean preferencial) {
        this.sucursalId = sucursalId;
        this.estacionId = estacionId;
        this.tramiteIds = tramiteIds != null ? tramiteIds : new ArrayList<>();
        this.preferencial = preferencial;
    }

    public boolean esEstacionFuncionario() {
        return estacionId != null && !estacionId.isBlank();
    }

    public String getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(String sucursalId) {
        this.sucursalId = sucursalId;
    }

    public String getEstacionId() {
        return estacionId;
    }

    public void setEstacionId(String estacionId) {
        this.estacionId = estacionId;
    }

    public List<String> getTramiteIds() {
        return tramiteIds;
    }

    public void setTramiteIds(List<String> tramiteIds) {
        this.tramiteIds = tramiteIds != null ? tramiteIds : new ArrayList<>();
    }

    public boolean isPreferencial() {
        return preferencial;
    }

    public void setPreferencial(boolean preferencial) {
        this.preferencial = preferencial;
    }

    @Override
    public String toString() {
        return "ConfiguracionLocal{sucursalId='" + sucursalId + "', estacionId='" + estacionId + "', tramiteIds=" + tramiteIds + "}";
    }
}
