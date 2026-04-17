package cr.ac.una.astroline.service;

import cr.ac.una.astroline.model.Tramite;
import cr.ac.una.astroline.util.DataNotifier;
import cr.ac.una.astroline.util.GsonUtil;
import cr.ac.una.astroline.util.Respuesta;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Servicio singleton para la gestión persistente de trámites.
 * Reactivo y sincronizado entre peers via DataNotifier.
 *
 * @author JohanDanilo
 */
public class TramiteService implements DataNotifier.Listener {

    private static final String ARCHIVO_JSON = "tramites.json";
    private final ObservableList<Tramite> listaDeTramites;
    private static TramiteService instancia;

    private TramiteService() {
        listaDeTramites = FXCollections.observableArrayList();
        cargarTramites();
        DataNotifier.subscribe(this);
    }

    public static TramiteService getInstancia() {
        if (instancia == null) {
            instancia = new TramiteService();
        }
        return instancia;
    }

    public ObservableList<Tramite> getListaDeTramites() {
        return listaDeTramites;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    public Tramite buscarPorId(String tramiteId) {
        if (tramiteId == null) return null;
        for (Tramite t : listaDeTramites) {
            if (t.getId().equals(tramiteId)) return t;
        }
        return null;
    }

    public boolean existe(String tramiteId) {
        return buscarPorId(tramiteId) != null;
    }

    public List<Tramite> getTramitesActivos() {
        List<Tramite> activos = new ArrayList<>();
        for (Tramite t : listaDeTramites) {
            if (t.isActivo()) activos.add(t);
        }
        return activos;
    }

    /**
     * Genera el siguiente ID disponible para un nuevo trámite (A-Z, luego T-NNN).
     */
    public String generarSiguienteId() {
        for (char letra = 'A'; letra <= 'Z'; letra++) {
            String candidato = String.valueOf(letra);
            if (!existe(candidato)) return candidato;
        }
        return String.format("T-%03d", listaDeTramites.size() + 1);
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public Respuesta agregar(Tramite tramite) {
        try {
            if (tramite == null)
                return new Respuesta(false, "El trámite no puede ser nulo.", "");
            if (tramite.getId() == null || tramite.getId().isBlank())
                return new Respuesta(false, "El ID del trámite es obligatorio.", "");
            if (existe(tramite.getId()))
                return new Respuesta(false, "Ya existe un trámite con ese id.", "");

            listaDeTramites.add(tramite);
            guardar();
            return new Respuesta(true, "Trámite agregado.", "", "tramite", tramite);
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo agregar el trámite.",
                    "TramiteService.agregar > " + e.getMessage());
        }
    }

    public Respuesta actualizar(Tramite tramiteActualizado) {
        try {
            if (tramiteActualizado == null)
                return new Respuesta(false, "El trámite no puede ser nulo.", "");

            for (int i = 0; i < listaDeTramites.size(); i++) {
                if (listaDeTramites.get(i).getId().equals(tramiteActualizado.getId())) {
                    listaDeTramites.set(i, tramiteActualizado);
                    guardar();
                    return new Respuesta(true, "Trámite actualizado.",
                            "", "tramite", tramiteActualizado);
                }
            }
            return new Respuesta(false, "Trámite no encontrado.", "");
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo actualizar el trámite.",
                    "TramiteService.actualizar > " + e.getMessage());
        }
    }

    public Respuesta eliminar(String tramiteId) {
        try {
            Tramite encontrado = buscarPorId(tramiteId);
            if (encontrado == null)
                return new Respuesta(false, "Trámite no encontrado.", "");

            listaDeTramites.remove(encontrado);
            guardar();
            return new Respuesta(true, "Trámite eliminado.", "");
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo eliminar el trámite.",
                    "TramiteService.eliminar > " + e.getMessage());
        }
    }

    public Respuesta cambiarEstado(String tramiteId, boolean activo) {
        try {
            Tramite encontrado = buscarPorId(tramiteId);
            if (encontrado == null)
                return new Respuesta(false, "Trámite no encontrado.", "");

            encontrado.setActivo(activo);
            guardar();
            return new Respuesta(true,
                    "Trámite " + (activo ? "activado" : "desactivado") + ".",
                    "", "tramite", encontrado);
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo cambiar el estado.",
                    "TramiteService.cambiarEstado > " + e.getMessage());
        }
    }

    // ── Carga inicial ────────────────────────────────────────────────────────

    private void cargarTramites() {
        List<Tramite> lista = GsonUtil.leerLista(ARCHIVO_JSON, Tramite.class);
        if (lista == null) lista = new ArrayList<>();
        listaDeTramites.setAll(lista);
    }

    // ── Reactividad P2P ───────────────────────────────────────────────────────

    @Override
    public void onDataChanged(String fileName) {
        if (!ARCHIVO_JSON.equals(fileName)) return;

        System.out.println("[TramiteService] Detectado cambio externo, sincronizando...");

        Platform.runLater(() -> {
            List<Tramite> remotos = GsonUtil.leerLista(ARCHIVO_JSON, Tramite.class);
            if (remotos != null) mergeTramites(remotos);
        });
    }

    /**
     * Merge por ID. Trámites no tienen lastModified porque el admin es la
     * única fuente de verdad — no hay conflictos concurrentes esperados.
     * La lista remota reemplaza la local si viene de una escritura del admin.
     *
     * Estrategia: la lista remota es autoritativa. Se conservan trámites
     * locales que no estén en la remota (evita borrados por race condition
     * en el polling de 15 segundos).
     */
    private void mergeTramites(List<Tramite> remotos) {
        // Construir mapa remoto por ID
        java.util.Map<String, Tramite> mapaRemoto = new java.util.LinkedHashMap<>();
        for (Tramite r : remotos) mapaRemoto.put(r.getId(), r);

        // Construir mapa local por ID
        java.util.Map<String, Tramite> mapaLocal = new java.util.LinkedHashMap<>();
        for (Tramite t : listaDeTramites) mapaLocal.put(t.getId(), t);

        // Remoto gana en colisión (es el admin quien escribe)
        mapaLocal.putAll(mapaRemoto);

        // Eliminar los que el remoto ya no tiene (borrado propagado)
        mapaLocal.keySet().retainAll(mapaRemoto.keySet());

        listaDeTramites.setAll(mapaLocal.values());
    }

    // ── Privados ──────────────────────────────────────────────────────────────

    private void guardar() {
        GsonUtil.guardarYPropagar(new ArrayList<>(listaDeTramites), ARCHIVO_JSON);
    }
}