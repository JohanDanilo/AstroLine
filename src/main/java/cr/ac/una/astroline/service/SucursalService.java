package cr.ac.una.astroline.service;

import cr.ac.una.astroline.model.Estacion;
import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.util.GsonUtil;
import cr.ac.una.astroline.util.Respuesta;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Servicio singleton para la gestión persistente de sucursales y estaciones.
 * Es la fuente de verdad para toda la estructura física del sistema.
 *
 * @author JohanDanilo
 */
public class SucursalService {

    private static final String ARCHIVO_JSON = "sucursales.json";
    private final ObservableList<Sucursal> listaDeSucursales;
    private static SucursalService instancia;

    private SucursalService() {
        listaDeSucursales = FXCollections.observableArrayList();
    }

    public static SucursalService getInstancia() {
        if (instancia == null) {
            instancia = new SucursalService();
            instancia.cargarSucursales();
        }
        return instancia;
    }

    public ObservableList<Sucursal> getListaDeSucursales() {
        return listaDeSucursales;
    }

    // ── Sucursales ────────────────────────────────────────────────────────────

    /**
     * Busca una sucursal por su id.
     *
     * @param sucursalId id de la sucursal
     * @return la Sucursal encontrada o null
     */
    public Sucursal buscarSucursal(String sucursalId) {
        if (sucursalId == null) return null;
        for (Sucursal s : listaDeSucursales) {
            if (s.getId().equals(sucursalId)) return s;
        }
        return null;
    }

    public boolean existeSucursal(String sucursalId) {
        return buscarSucursal(sucursalId) != null;
    }

    /**
     * Agrega una nueva sucursal al sistema.
     *
     * @param sucursal la sucursal a agregar
     * @return Respuesta con estado del resultado
     */
    public Respuesta agregarSucursal(Sucursal sucursal) {
        try {
            if (sucursal == null)
                return new Respuesta(false, "La sucursal no puede ser nula.", "");
            if (existeSucursal(sucursal.getId()))
                return new Respuesta(false, "Ya existe una sucursal con ese id.", "");

            listaDeSucursales.add(sucursal);
            guardar();
            return new Respuesta(true, "Sucursal agregada.", "", "sucursal", sucursal);
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo agregar la sucursal.",
                    "SucursalService.agregarSucursal > " + e.getMessage());
        }
    }

    /**
     * Actualiza los datos generales de una sucursal (nombre, textoAviso).
     * No toca las estaciones — eso se maneja por separado.
     *
     * @param sucursalActualizada sucursal con los nuevos datos
     * @return Respuesta con estado del resultado
     */
    public Respuesta actualizarSucursal(Sucursal sucursalActualizada) {
        try {
            if (sucursalActualizada == null)
                return new Respuesta(false, "La sucursal no puede ser nula.", "");

            for (int i = 0; i < listaDeSucursales.size(); i++) {
                if (listaDeSucursales.get(i).getId().equals(sucursalActualizada.getId())) {
                    listaDeSucursales.set(i, sucursalActualizada);
                    guardar();
                    return new Respuesta(true, "Sucursal actualizada.",
                            "", "sucursal", sucursalActualizada);
                }
            }
            return new Respuesta(false, "Sucursal no encontrada.", "");
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo actualizar la sucursal.",
                    "SucursalService.actualizarSucursal > " + e.getMessage());
        }
    }

    /**
     * Elimina una sucursal y todas sus estaciones.
     *
     * @param sucursalId id de la sucursal a eliminar
     * @return Respuesta con estado del resultado
     */
    public Respuesta eliminarSucursal(String sucursalId) {
        try {
            Sucursal encontrada = buscarSucursal(sucursalId);
            if (encontrada == null)
                return new Respuesta(false, "Sucursal no encontrada.", "");

            listaDeSucursales.remove(encontrada);
            guardar();
            return new Respuesta(true, "Sucursal eliminada.", "");
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo eliminar la sucursal.",
                    "SucursalService.eliminarSucursal > " + e.getMessage());
        }
    }

    // ── Estaciones ────────────────────────────────────────────────────────────

    /**
     * Busca una estación en cualquier sucursal por su id.
     * Útil para cuando Ficha solo tiene estacionId sin saber en qué sucursal.
     *
     * @param estacionId id de la estación
     * @return la Estacion encontrada o null
     */
    public Estacion buscarEstacion(String estacionId) {
        if (estacionId == null) return null;
        for (Sucursal s : listaDeSucursales) {
            Estacion e = s.buscarEstacion(estacionId);
            if (e != null) return e;
        }
        return null;
    }

    /**
     * Agrega una estación a una sucursal específica.
     *
     * @param sucursalId id de la sucursal destino
     * @param estacion   la estación a agregar
     * @return Respuesta con estado del resultado
     */
    public Respuesta agregarEstacion(String sucursalId, Estacion estacion) {
        try {
            Sucursal sucursal = buscarSucursal(sucursalId);
            if (sucursal == null)
                return new Respuesta(false, "Sucursal no encontrada.", "");
            if (estacion == null)
                return new Respuesta(false, "La estación no puede ser nula.", "");

            boolean agregada = sucursal.agregarEstacion(estacion);
            if (!agregada)
                return new Respuesta(false, "Ya existe una estación con ese id.", "");

            guardar();
            return new Respuesta(true, "Estación agregada.", "", "estacion", estacion);
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo agregar la estación.",
                    "SucursalService.agregarEstacion > " + e.getMessage());
        }
    }

    /**
     * Actualiza los datos de una estación dentro de su sucursal.
     *
     * @param sucursalId         id de la sucursal que contiene la estación
     * @param estacionActualizada estación con los nuevos datos
     * @return Respuesta con estado del resultado
     */
    public Respuesta actualizarEstacion(String sucursalId, Estacion estacionActualizada) {
        try {
            Sucursal sucursal = buscarSucursal(sucursalId);
            if (sucursal == null)
                return new Respuesta(false, "Sucursal no encontrada.", "");
            if (estacionActualizada == null)
                return new Respuesta(false, "La estación no puede ser nula.", "");

            List<Estacion> estaciones = sucursal.getEstaciones();
            for (int i = 0; i < estaciones.size(); i++) {
                if (estaciones.get(i).getId().equals(estacionActualizada.getId())) {
                    estaciones.set(i, estacionActualizada);
                    guardar();
                    return new Respuesta(true, "Estación actualizada.",
                            "", "estacion", estacionActualizada);
                }
            }
            return new Respuesta(false, "Estación no encontrada en la sucursal.", "");
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo actualizar la estación.",
                    "SucursalService.actualizarEstacion > " + e.getMessage());
        }
    }

    /**
     * Elimina una estación de una sucursal específica.
     *
     * @param sucursalId id de la sucursal
     * @param estacionId id de la estación a eliminar
     * @return Respuesta con estado del resultado
     */
    public Respuesta eliminarEstacion(String sucursalId, String estacionId) {
        try {
            Sucursal sucursal = buscarSucursal(sucursalId);
            if (sucursal == null)
                return new Respuesta(false, "Sucursal no encontrada.", "");

            boolean eliminada = sucursal.eliminarEstacion(estacionId);
            if (!eliminada)
                return new Respuesta(false, "Estación no encontrada.", "");

            guardar();
            return new Respuesta(true, "Estación eliminada.", "");
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo eliminar la estación.",
                    "SucursalService.eliminarEstacion > " + e.getMessage());
        }
    }

    // ── Privados ──────────────────────────────────────────────────────────────

    private void guardar() {
        GsonUtil.guardar(new ArrayList<>(listaDeSucursales), ARCHIVO_JSON);
    }

    private void cargarSucursales() {
        List<Sucursal> lista = GsonUtil.leerLista(ARCHIVO_JSON, Sucursal.class);
        if (lista == null) lista = new ArrayList<>();
        listaDeSucursales.setAll(lista);
    }
}