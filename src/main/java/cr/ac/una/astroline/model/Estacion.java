package cr.ac.una.astroline.model;

import java.util.ArrayList;
import java.util.List;

public class Estacion {

    private String id;
    private String nombre;
    private String sucursalId;
    private boolean preferencial;
    private boolean estaActiva;
    private List<String> tramiteIds;

    public Estacion() {
        this.tramiteIds = new ArrayList<>();
    }

    public Estacion(String id, String nombre, String sucursalId, boolean preferencial, boolean estaActiva) {
        this.id = id;
        this.nombre = nombre;
        this.sucursalId = sucursalId;
        this.preferencial = preferencial;
        this.estaActiva = estaActiva;
        this.tramiteIds = new ArrayList<>();
    }

    public boolean atiendeTramite(String tramiteId) {
        return tramiteIds != null && tramiteIds.contains(tramiteId);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(String sucursalId) {
        this.sucursalId = sucursalId;
    }

    public boolean isPreferencial() {
        return preferencial;
    }

    public void setPreferencial(boolean preferencial) {
        this.preferencial = preferencial;
    }

    public boolean isEstaActiva() {
        return estaActiva;
    }

    public void setEstaActiva(boolean estaActiva) {
        this.estaActiva = estaActiva;
    }

    public List<String> getTramiteIds() {
        return tramiteIds;
    }

    public void setTramiteIds(List<String> tramiteIds) {
        this.tramiteIds = tramiteIds != null ? tramiteIds : new ArrayList<>();
    }

    public boolean agregarTramite(String tramiteId) {
        if (tramiteIds == null) {
            tramiteIds = new ArrayList<>();
        }
        if (tramiteId == null || atiendeTramite(tramiteId)) {
            return false;
        }
        tramiteIds.add(tramiteId);
        return true;
    }

    public boolean quitarTramite(String tramiteId) {
        return tramiteIds != null && tramiteIds.remove(tramiteId);
    }

    public Estacion clonarEstacion(Estacion original) {
        if (original == null) {
            return null;
        }

        Estacion copia = new Estacion(original.getId(), original.getNombre(), original.getSucursalId(), original.isPreferencial(), original.isEstaActiva());
        copia.setTramiteIds(new ArrayList<>(original.getTramiteIds()));
        return copia;
    }

    @Override
    public String toString() {
        return "Estacion{id='" + id + "', nombre='" + nombre + "', preferencial=" + preferencial + "}";
    }
}
