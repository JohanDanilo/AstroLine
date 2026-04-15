package cr.ac.una.astroline.service;

import cr.ac.una.astroline.model.Tramite;
import cr.ac.una.astroline.util.GsonUtil;
import cr.ac.una.astroline.util.Respuesta;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Servicio singleton para la gestión persistente de trámites.
 * Es la fuente de verdad del catálogo de trámites del sistema.
 *
 * @author JohanDanilo
 */
public class TramiteService {

    private static final String ARCHIVO_JSON = "tramites.json";
    private final ObservableList<Tramite> listaDeTramites;
    private static TramiteService instancia;

    private TramiteService() {
        listaDeTramites = FXCollections.observableArrayList();
    }

    public static TramiteService getInstancia() {
        if (instancia == null) {
            instancia = new TramiteService();
            instancia.cargarTramites();
        }
        return instancia;
    }

    public ObservableList<Tramite> getListaDeTramites() {
        return listaDeTramites;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    /**
     * Busca un trámite por su id.
     *
     * @param tramiteId id del trámite (ej: "A", "B", "C")
     * @return el Tramite encontrado o null
     */
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

    /**
     * Retorna solo los trámites activos.
     * Es lo que el Kiosko usa para mostrar opciones al cliente.
     *
     * @return lista de trámites con activo = true
     */
    public List<Tramite> getTramitesActivos() {
        List<Tramite> activos = new ArrayList<>();
        for (Tramite t : listaDeTramites) {
            if (t.isActivo()) activos.add(t);
        }
        return activos;
    }

    /**
     * Genera el siguiente ID disponible para un nuevo trámite.
     *
     * Los IDs siguen la convención de letra única: A, B, C... Z.
     * Recorre el alfabeto y retorna la primera letra que no esté en uso.
     * Si todas las letras (A-Z) están ocupadas, genera un ID numérico
     * con formato "T-NNN" basado en el tamaño actual de la lista.
     *
     * Ejemplo:
     *   Lista actual: [A, B, C] → retorna "D"
     *   Lista actual: [A, C]    → retorna "B" (primer hueco)
     *   Lista actual: [A..Z]    → retorna "T-027"
     *
     * @return id sugerido para el siguiente trámite
     */
    public String generarSiguienteId() {
        for (char letra = 'A'; letra <= 'Z'; letra++) {
            String candidato = String.valueOf(letra);
            if (!existe(candidato)) {
                return candidato;
            }
        }
        // Fallback numérico si las 26 letras están ocupadas
        return String.format("T-%03d", listaDeTramites.size() + 1);
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    /**
     * Agrega un nuevo trámite al catálogo.
     *
     * @param tramite el trámite a agregar
     * @return Respuesta con estado del resultado
     */
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

    /**
     * Actualiza los datos de un trámite existente.
     *
     * @param tramiteActualizado trámite con los nuevos datos
     * @return Respuesta con estado del resultado
     */
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

    /**
     * Elimina un trámite del catálogo por su id.
     * Precaución: no verifica si estaciones lo tienen asignado.
     * Esa validación es responsabilidad del controlador.
     *
     * @param tramiteId id del trámite a eliminar
     * @return Respuesta con estado del resultado
     */
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

    /**
     * Cambia el estado activo/inactivo de un trámite sin reemplazar el objeto.
     * Más limpio que actualizar el objeto completo solo por el estado.
     *
     * @param tramiteId id del trámite
     * @param activo    nuevo estado
     * @return Respuesta con estado del resultado
     */
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

    // ── Privados ──────────────────────────────────────────────────────────────

    /**
     * Guarda la lista de trámites en disco y la propaga a los peers en red.
     * Usa guardarYPropagar para que todos los módulos en LAN se actualicen.
     */
    private void guardar() {
        GsonUtil.guardarYPropagar(new ArrayList<>(listaDeTramites), ARCHIVO_JSON);
    }

    private void cargarTramites() {
        List<Tramite> lista = GsonUtil.leerLista(ARCHIVO_JSON, Tramite.class);
        if (lista == null) lista = new ArrayList<>();
        listaDeTramites.setAll(lista);
    }
}