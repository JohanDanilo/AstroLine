package cr.ac.una.astroline.service;

import cr.ac.una.astroline.model.Estacion;
import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.util.DataNotifier;
import cr.ac.una.astroline.util.GsonUtil;
import cr.ac.una.astroline.util.Respuesta;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Servicio singleton para la gestión persistente de sucursales y estaciones.
 * Reactivo y sincronizado entre peers via DataNotifier.
 * Estaciones viven embebidas dentro de cada Sucursal en sucursales.json.
 *
 * @author JohanDanilo
 */
public class SucursalService implements DataNotifier.Listener {

    private static final String ARCHIVO_JSON = "sucursales.json";
    private final ObservableList<Sucursal> listaDeSucursales;
    private static SucursalService instancia;

    private SucursalService() {
        listaDeSucursales = FXCollections.observableArrayList();
        cargarSucursales();
        DataNotifier.subscribe(this);
    }

    public static SucursalService getInstancia() {
        if (instancia == null) {
            instancia = new SucursalService();
        }
        return instancia;
    }

    public ObservableList<Sucursal> getListaDeSucursales() {
        return listaDeSucursales;
    }

    // ── Sucursales ────────────────────────────────────────────────────────────

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
     * Útil para cuando Ficha solo tiene estacionId.
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
     * Retorna todas las estaciones de una sucursal específica.
     */
    public List<Estacion> getEstacionesDeSucursal(String sucursalId) {
        Sucursal s = buscarSucursal(sucursalId);
        return s != null ? s.getEstaciones() : new ArrayList<>();
    }

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

    /**
     * Genera un ID único para una nueva estación dentro de una sucursal.
     * Formato: "estacion-N" donde N es incremental global entre todas las sucursales.
     */
    public String generarIdEstacion() {
        int consecutivoMaximo = 0;
        for (Sucursal sucursal : listaDeSucursales) {
            for (Estacion estacion : sucursal.getEstaciones()) {
                consecutivoMaximo = Math.max(
                        consecutivoMaximo,
                        extraerConsecutivo(estacion.getId(), "estacion-")
                );
            }
        }

        String candidato;
        do {
            consecutivoMaximo++;
            candidato = "estacion-" + consecutivoMaximo;
        } while (buscarEstacion(candidato) != null);

        return candidato;
    }

    /**
     * Genera un ID único para una nueva sucursal.
     * Formato: "sucursal-N".
     */
    public String generarIdSucursal() {
        int consecutivoMaximo = 0;
        for (Sucursal sucursal : listaDeSucursales) {
            consecutivoMaximo = Math.max(
                    consecutivoMaximo,
                    extraerConsecutivo(sucursal.getId(), "sucursal-")
            );
        }

        String candidato;
        do {
            consecutivoMaximo++;
            candidato = "sucursal-" + consecutivoMaximo;
        } while (existeSucursal(candidato));

        return candidato;
    }

    // ── Carga inicial ─────────────────────────────────────────────────────────

    private void cargarSucursales() {
        List<Sucursal> lista = GsonUtil.leerLista(ARCHIVO_JSON, Sucursal.class);
        if (lista == null) lista = new ArrayList<>();
        listaDeSucursales.setAll(lista);
    }

    // ── Reactividad P2P ───────────────────────────────────────────────────────

    /**
     * Llamado por DataNotifier cuando SyncServer recibe sucursales.json de un peer.
     * Ejecutado desde hilo HTTP — requiere Platform.runLater para tocar la UI.
     */
    @Override
    public void onDataChanged(String fileName) {
        if (!ARCHIVO_JSON.equals(fileName)) return;

        System.out.println("[SucursalService] Detectado cambio externo, sincronizando...");

        Platform.runLater(() -> {
            List<Sucursal> remotas = GsonUtil.leerLista(ARCHIVO_JSON, Sucursal.class);
            if (remotas != null) mergeSucursales(remotas);
        });
    }

    /**
     * Merge por ID de sucursal.
     * La lista remota es autoritativa (solo el admin modifica sucursales).
     * Se conserva cualquier sucursal local que no esté en el remoto como
     * protección ante race conditions del poller de 15 segundos.
     *
     * Estaciones se reemplazan completas con las del remoto —
     * viven embebidas y no tienen lastModified propio.
     */
    private void mergeSucursales(List<Sucursal> remotas) {
        java.util.Map<String, Sucursal> mapaRemoto = new java.util.LinkedHashMap<>();
        for (Sucursal r : remotas) mapaRemoto.put(r.getId(), r);

        java.util.Map<String, Sucursal> mapaLocal = new java.util.LinkedHashMap<>();
        for (Sucursal s : listaDeSucursales) mapaLocal.put(s.getId(), s);

        // Remoto gana en colisión
        mapaLocal.putAll(mapaRemoto);

        // Eliminar sucursales que el remoto borró
        mapaLocal.keySet().retainAll(mapaRemoto.keySet());

        listaDeSucursales.setAll(mapaLocal.values());
    }

    // ── Privados ──────────────────────────────────────────────────────────────

    private void guardar() {
        GsonUtil.guardarYPropagar(new ArrayList<>(listaDeSucursales), ARCHIVO_JSON);
    }

    private int extraerConsecutivo(String id, String prefijo) {
        if (id == null || !id.startsWith(prefijo)) {
            return 0;
        }

        try {
            return Integer.parseInt(id.substring(prefijo.length()));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
