package cr.ac.una.astroline.service;


import cr.ac.una.astroline.model.Estacion;
import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.model.Tramite;
import cr.ac.una.astroline.util.DataNotifier;
import cr.ac.una.astroline.util.GsonUtil;
import cr.ac.una.astroline.util.Respuesta;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Servicio singleton para la gestión persistente de sucursales y estaciones.
 * Reactivo y sincronizado entre peers via DataNotifier.
 * Estaciones viven embebidas dentro de cada Sucursal en sucursales.json.
 *
 * Regla de merge P2P: por cada sucursal con el mismo id, gana la que
 * tenga el lastModified más alto. Las sucursales que sólo existen en
 * local se conservan (protección ante race conditions del poller de 15 s).
 * Las sucursales que sólo existen en el remoto se agregan.
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

            sucursal.marcarModificada();
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
                    sucursalActualizada.marcarModificada();
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

            sucursal.marcarModificada(); // estacion embebida → toca a la sucursal padre
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
                    sucursal.marcarModificada(); // estacion embebida → toca a la sucursal padre
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

            sucursal.marcarModificada(); // estacion embebida → toca a la sucursal padre
            guardar();
            return new Respuesta(true, "Estación eliminada.", "");
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo eliminar la estación.",
                    "SucursalService.eliminarEstacion > " + e.getMessage());
        }
    }

    /**
    * Genera un ID único para una nueva estación dentro de una sucursal específica.
    * Formato: "E-{numSucursal}-{numEstacion}"
    * Ejemplo: E-1-1 = primera estación de sucursal-1
    *          E-2-3 = tercera estación de sucursal-2
    *
    * El contador es LOCAL a la sucursal: cada una tiene su propia secuencia.
    * Esto hace el ID legible y trazable sin ambigüedad.
    *
    * @param sucursalId id de la sucursal donde se creará la estación (ej: "sucursal-1")
    * @return id único para la nueva estación, o null si la sucursal no existe
    */
    public String generarIdEstacion(String sucursalId) {
       Sucursal sucursal = buscarSucursal(sucursalId);
       if (sucursal == null) return null;

       int numSucursal = extraerConsecutivo(sucursalId, "sucursal-");
       String prefijo = "E-" + numSucursal + "-";

       int maxEstacion = 0;
       for (Estacion e : sucursal.getEstaciones()) {
           maxEstacion = Math.max(maxEstacion, extraerConsecutivo(e.getId(), prefijo));
       }

       String candidato;
       do {
           maxEstacion++;
           candidato = prefijo + maxEstacion;
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
     * Merge por ID de sucursal con resolución de conflictos por lastModified.
     *
     * Reglas:
     * - Sucursal presente en ambos lados: gana la que tenga lastModified más alto.
     * - Sucursal solo en remoto: se agrega (fue creada en otra máquina).
     * - Sucursal solo en local: se conserva (puede haberse creado aquí y aún
     *   no propagado, o el poller llegó antes de que la propagación terminara).
     *
     * Las estaciones se reemplazan completas con las de la sucursal ganadora
     * porque viven embebidas y no tienen lastModified propio.
     */
    private void mergeSucursales(List<Sucursal> remotas) {
        Map<String, Sucursal> mapaRemoto = new LinkedHashMap<>();
        for (Sucursal r : remotas) mapaRemoto.put(r.getId(), r);

        Map<String, Sucursal> mapaLocal = new LinkedHashMap<>();
        for (Sucursal s : listaDeSucursales) mapaLocal.put(s.getId(), s);

        // Resultado final: empezamos con todas las remotas como base
        Map<String, Sucursal> resultado = new LinkedHashMap<>(mapaRemoto);

        // Revisar cada sucursal local
        for (Map.Entry<String, Sucursal> entradaLocal : mapaLocal.entrySet()) {
            String id = entradaLocal.getKey();
            Sucursal local = entradaLocal.getValue();
            Sucursal remota = mapaRemoto.get(id);

            if (remota == null) {
                // Solo existe en local → conservar (aún no propagada o race condition)
                resultado.put(id, local);
            } else {
                // Existe en ambos → gana la más reciente por lastModified
                if (local.getLastModified() > remota.getLastModified()) {
                    resultado.put(id, local);
                }
                // Si remota es más nueva o igual, ya está en resultado por el putAll inicial
            }
        }

        listaDeSucursales.setAll(resultado.values());
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
    
    public List<Tramite> getTramitesDeEstacion(String estacionId) {
        Estacion estacion = buscarEstacion(estacionId);

        if (estacion == null || estacion.getTramiteIds() == null) {
            return new ArrayList<>();
        }

        List<Tramite> tramites = new ArrayList<>();

        for (String tramiteId : estacion.getTramiteIds()) {
            Tramite tramite = TramiteService.getInstancia().buscarPorId(tramiteId);
            if (tramite != null) {
                tramites.add(tramite);
            }
        }

        return tramites;
    }
}