package cr.ac.una.astroline.model;

import java.util.ArrayList;
import java.util.List;

public class Sucursal {

    private String id;
    private String nombre;
    private String textoAviso;
    private List<Estacion> estaciones;
    private long lastModified;

    public Sucursal() {
        this.estaciones = new ArrayList<>();
        this.textoAviso = "";
        this.lastModified = System.currentTimeMillis();
    }

    public Sucursal(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        this.textoAviso = "";
        this.estaciones = new ArrayList<>();
        this.lastModified = System.currentTimeMillis();
    }

    public Estacion buscarEstacion(String estacionId) {
        if (estacionId == null) {
            return null;
        }
        for (Estacion e : estaciones) {
            if (e.getId().equals(estacionId)) {
                return e;
            }
        }
        return null;
    }

    public boolean agregarEstacion(Estacion estacion) {
        if (estacion == null || buscarEstacion(estacion.getId()) != null) {
            return false;
        }
        estaciones.add(estacion);
        return true;
    }

    public boolean eliminarEstacion(String estacionId) {
        Estacion encontrada = buscarEstacion(estacionId);
        if (encontrada == null) {
            return false;
        }
        estaciones.remove(encontrada);
        return true;
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

    public String getTextoAviso() {
        return textoAviso;
    }

    public void setTextoAviso(String textoAviso) {
        this.textoAviso = textoAviso != null ? textoAviso : "";
    }

    public List<Estacion> getEstaciones() {
        return estaciones;
    }

    public void setEstaciones(List<Estacion> estaciones) {
        this.estaciones = estaciones != null ? estaciones : new ArrayList<>();
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public void marcarModificada() {
        this.lastModified = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "Sucursal{id='" + id + "', nombre='" + nombre + "', estaciones=" + estaciones.size() + ", lastModified=" + lastModified + "}";
    }
}
