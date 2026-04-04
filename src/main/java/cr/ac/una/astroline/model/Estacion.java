package cr.ac.una.astroline.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una estación de atención dentro de una sucursal.
 * Cada estación tiene asignados los trámites que puede atender.
 * Se persiste como parte de data/sucursales.json
 *
 * @author JohanDanilo
 */
public class Estacion {

    private String id;
    private String nombre;
    private String sucursalId;
    private boolean preferencial;
    private boolean estaActiva;
    private List<String> tramiteIds; // IDs de los trámites que atiende

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

    /**
     * Verifica si esta estación atiende un trámite específico.
     *
     * @param tramiteId el ID del trámite a verificar
     * @return true si la estación atiende ese trámite
     */
    public boolean atiendeTramite(String tramiteId) {
        return tramiteIds.contains(tramiteId);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getSucursalId() { return sucursalId; }
    public void setSucursalId(String sucursalId) { this.sucursalId = sucursalId; }

    public boolean isPreferencial() { return preferencial; }
    public void setPreferencial(boolean preferencial) { this.preferencial = preferencial; }

    public boolean isEstaActiva() { return estaActiva; }
    public void setEstaActiva(boolean estaActiva) { this.estaActiva = estaActiva; }
    
    public List<String> getTramiteIds() { return tramiteIds; }
    public void setTramiteIds(List<String> tramiteIds) { this.tramiteIds = tramiteIds; }

    /**
     * Agrega un trámite a los que atiende esta estación.
     * Evita duplicados.
     * Usado por el drag and drop del módulo de administrador.
     *
     * @param tramiteId id del trámite a agregar
     * @return true si se agregó, false si ya existía
     */
    public boolean agregarTramite(String tramiteId) {
        if (tramiteId == null || atiendeTramite(tramiteId)) return false;
        tramiteIds.add(tramiteId);
        return true;
    }

    /**
     * Quita un trámite de los que atiende esta estación.
     * Usado por el drag and drop del módulo de administrador.
     *
     * @param tramiteId id del trámite a quitar
     * @return true si se quitó, false si no existía
     */
    public boolean quitarTramite(String tramiteId) {
        return tramiteIds.remove(tramiteId);
    }

    @Override
    public String toString() {
        return "Estacion{id='" + id + "', nombre='" + nombre + 
               "', preferencial=" + preferencial + "}";
    }
}