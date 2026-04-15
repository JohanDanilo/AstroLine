package cr.ac.una.astroline.service;

import cr.ac.una.astroline.model.Ficha;
import cr.ac.una.astroline.util.GsonUtil;
import cr.ac.una.astroline.util.Respuesta;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de negocio para la gestión de fichas.
 * Maneja generación, persistencia y archivado automático al historial.
 *
 * @author AstroLine
 */
public class FichaService{

    private static final String ARCHIVO_FICHAS    = "fichas.json";
    private static final String ARCHIVO_HISTORIAL = "historial.json";
    private static final ZoneId ZONA_CR           = ZoneId.of("America/Costa_Rica");
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // -------------------------------------------------------------------------
    // GENERACIÓN DE FICHA
    // -------------------------------------------------------------------------

    /**
     * Genera una nueva ficha para el trámite indicado.
     * Si es la primera ficha del día, archiva las fichas anteriores primero.
     *
     * @param tramiteId      id del trámite (la letra: A, B, C...)
     * @param sucursalId     id de la sucursal
     * @param cedulaCliente  cédula del cliente, null si no se identificó
     * @param preferencial   true si tiene atención preferencial
     */
    public Respuesta generarFicha(String tramiteId, String sucursalId,
            String cedulaCliente, boolean preferencial) {
        try {
            List<Ficha> fichasActivas = GsonUtil.leerLista(ARCHIVO_FICHAS, Ficha.class);

            // Si hay fichas de un día anterior, archivarlas primero
            if (!fichasActivas.isEmpty() && esDeOtroDia(fichasActivas.get(0))) {
                Respuesta rArchivo = archivarFichas(fichasActivas);
                if (!rArchivo.getEstado()) return rArchivo;
                fichasActivas = new ArrayList<>();
            }

            // Calcular el siguiente número para este trámite
            int siguienteNumero = calcularSiguienteNumero(fichasActivas, tramiteId);

            // Construir el id único: letra + número + fecha (ej: A-001-28-03-2026)
            String fechaHoy = LocalDate.now(ZONA_CR).format(FORMATO_FECHA);
            String id = tramiteId + "-" + String.format("%03d", siguienteNumero) + "-" + fechaHoy;

            Ficha ficha = new Ficha(id, siguienteNumero, tramiteId,
                    sucursalId, cedulaCliente, preferencial);

            fichasActivas.add(ficha);
            GsonUtil.guardar(fichasActivas, ARCHIVO_FICHAS);

            return new Respuesta(true,
                    "Ficha generada: " + ficha.getCodigo(),
                    "", "ficha", ficha);

        } catch (Exception e) {
            return new Respuesta(false,
                    "No se pudo generar la ficha.",
                    "FichaService.generarFicha > " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // CONSULTAS
    // -------------------------------------------------------------------------

    /**
     * Retorna todas las fichas activas del día actual.
     */
    public Respuesta obtenerFichasActivas() {
        try {
            List<Ficha> lista = GsonUtil.leerLista(ARCHIVO_FICHAS, Ficha.class);
            return new Respuesta(true, "", "", "lista", lista);
        } catch (Exception e) {
            return new Respuesta(false,
                    "No se pudo obtener las fichas.",
                    "FichaService.obtenerFichasActivas > " + e.getMessage());
        }
    }

    /**
     * Retorna las últimas n fichas del historial (para el módulo de funcionarios).
     *
     * @param cantidad cantidad de fichas a retornar
     */
    public Respuesta obtenerUltimasDelHistorial(int cantidad) {
        try {
            List<Ficha> historial = GsonUtil.leerLista(ARCHIVO_HISTORIAL, Ficha.class);

            // Tomar las últimas 'cantidad' fichas
            int desde = Math.max(0, historial.size() - cantidad);
            List<Ficha> ultimas = historial.subList(desde, historial.size());

            return new Respuesta(true, "", "", "lista", new ArrayList<>(ultimas));
        } catch (Exception e) {
            return new Respuesta(false,
                    "No se pudo obtener el historial.",
                    "FichaService.obtenerUltimasDelHistorial > " + e.getMessage());
        }
    }

    /**
     * Actualiza el estado de una ficha existente en fichas.json.
     * Usado por el módulo de funcionarios para marcarla ATENDIDA, etc.
     *
     * @param fichaId id de la ficha a actualizar
     * @param estado  nuevo estado
     */
    public Respuesta actualizarEstado(String fichaId, Ficha.Estado estado) {
        try {
            List<Ficha> fichas = GsonUtil.leerLista(ARCHIVO_FICHAS, Ficha.class);

            Ficha encontrada = fichas.stream()
                    .filter(f -> f.getId().equals(fichaId))
                    .findFirst()
                    .orElse(null);

            if (encontrada == null) {
                return new Respuesta(false, "Ficha no encontrada.", "");
            }

            encontrada.setEstado(estado);
            GsonUtil.guardar(fichas, ARCHIVO_FICHAS);

            return new Respuesta(true, "Estado actualizado.", "", "ficha", encontrada);

        } catch (Exception e) {
            return new Respuesta(false,
                    "No se pudo actualizar la ficha.",
                    "FichaService.actualizarEstado > " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // MÉTODOS PRIVADOS
    // -------------------------------------------------------------------------

    /**
     * Verifica si una ficha fue emitida en un día diferente al de hoy.
     */
    private boolean esDeOtroDia(Ficha ficha) {
        String fechaHoy = LocalDate.now(ZONA_CR).format(FORMATO_FECHA);
        // fechaHoraEmision tiene formato "dd-MM-yyyy HH:mm:ss"
        // los primeros 10 caracteres son la fecha
        String fechaFicha = ficha.getFechaHoraEmision().substring(0, 10);
        return !fechaFicha.equals(fechaHoy);
    }

    /**
     * Calcula el siguiente número de ficha para un trámite específico en el día.
     * Cuenta cuántas fichas de ese trámite ya existen hoy y suma 1.
     */
    private int calcularSiguienteNumero(List<Ficha> fichasActivas, String tramiteId) {
        long cantidad = fichasActivas.stream()
                .filter(f -> f.getTramiteId().equals(tramiteId))
                .count();
        return (int) cantidad + 1;
    }

    /**
     * Mueve todas las fichas al historial.json y limpia fichas.json.
     */
    private Respuesta archivarFichas(List<Ficha> fichasAArchivar) {
        try {
            List<Ficha> historialExistente = GsonUtil.leerLista(ARCHIVO_HISTORIAL, Ficha.class);
            historialExistente.addAll(fichasAArchivar);
            GsonUtil.guardar(historialExistente, ARCHIVO_HISTORIAL);
            GsonUtil.guardar(new ArrayList<>(), ARCHIVO_FICHAS);
            System.out.println("[FichaService] " + fichasAArchivar.size()
                    + " fichas archivadas en historial.json");
            return new Respuesta(true, "", "");
        } catch (Exception e) {
            return new Respuesta(false,
                    "Error al archivar fichas.",
                    "FichaService.archivarFichas > " + e.getMessage());
        }
    }
}