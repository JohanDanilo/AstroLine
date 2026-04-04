package cr.ac.una.astroline.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una sucursal de atención.
 * Contiene una lista de estaciones propias y un texto de avisos
 * que se muestra corriendo en ciclo en la pantalla de Proyección.
 * Se persiste en data/sucursales.json
 *
 * @author JohanDanilo
 */
public class Sucursal {

    private String id;
    private String nombre;
    private String textoAviso; // se muestra en Proyección corriendo en ciclo
    private List<Estacion> estaciones;

    public Sucursal() {
        this.estaciones = new ArrayList<>();
        this.textoAviso = "";
    }

    public Sucursal(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        this.textoAviso = "";
        this.estaciones = new ArrayList<>();
    }

    /**
     * Busca una estación dentro de esta sucursal por su id.
     *
     * @param estacionId id de la estación
     * @return la Estacion encontrada o null si no existe
     */
    public Estacion buscarEstacion(String estacionId) {
        if (estacionId == null) return null;
        for (Estacion e : estaciones) {
            if (e.getId().equals(estacionId)) return e;
        }
        return null;
    }

    /**
     * Agrega una estación a esta sucursal.
     * No agrega duplicados por id.
     *
     * @param estacion la estación a agregar
     * @return true si se agregó, false si ya existía
     */
    public boolean agregarEstacion(Estacion estacion) {
        if (estacion == null || buscarEstacion(estacion.getId()) != null) return false;
        estaciones.add(estacion);
        return true;
    }

    /**
     * Elimina una estación de esta sucursal por su id.
     *
     * @param estacionId id de la estación a eliminar
     * @return true si se eliminó, false si no existía
     */
    public boolean eliminarEstacion(String estacionId) {
        Estacion encontrada = buscarEstacion(estacionId);
        if (encontrada == null) return false;
        estaciones.remove(encontrada);
        return true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTextoAviso() { return textoAviso; }
    public void setTextoAviso(String textoAviso) {
        this.textoAviso = textoAviso != null ? textoAviso : "";
    }

    public List<Estacion> getEstaciones() { return estaciones; }
    public void setEstaciones(List<Estacion> estaciones) {
        this.estaciones = estaciones != null ? estaciones : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Sucursal{id='" + id + "', nombre='" + nombre +
                "', estaciones=" + estaciones.size() + "}";
    }
}