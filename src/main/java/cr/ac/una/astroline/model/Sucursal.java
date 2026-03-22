package cr.ac.una.astroline.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una sucursal de la empresa.
 * Contiene las estaciones de atención y el texto de avisos para Proyección.
 * Se persiste en data/sucursales.json
 *
 * @author JohanDanilo
 */
public class Sucursal {

    private String id;
    private String nombre;
    private String direccion;
    private String textoAvisos; // texto que corre en la pantalla Proyección
    private List<Estacion> estaciones;

    public Sucursal() {
        this.estaciones = new ArrayList<>();
    }

    public Sucursal(String id, String nombre, String direccion, String textoAvisos) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.textoAvisos = textoAvisos;
        this.estaciones = new ArrayList<>();
    }

    /**
     * Busca una estación por su ID dentro de esta sucursal.
     *
     * @param estacionId el ID de la estación a buscar
     * @return la estación encontrada o null si no existe
     */
    public Estacion buscarEstacion(String estacionId) {
        for (Estacion estacion : estaciones) {
            if (estacion.getId().equals(estacionId)) {
                return estacion;
            }
        }
        return null;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTextoAvisos() { return textoAvisos; }
    public void setTextoAvisos(String textoAvisos) { this.textoAvisos = textoAvisos; }

    public List<Estacion> getEstaciones() { return estaciones; }
    public void setEstaciones(List<Estacion> estaciones) { this.estaciones = estaciones; }

    @Override
    public String toString() {
        return "Sucursal{id='" + id + "', nombre='" + nombre + "'}";
    }
}