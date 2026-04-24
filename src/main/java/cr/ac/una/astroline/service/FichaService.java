package cr.ac.una.astroline.service;

import cr.ac.una.astroline.model.Ficha;
import cr.ac.una.astroline.model.Tramite;
import cr.ac.una.astroline.util.GsonUtil;
import cr.ac.una.astroline.util.Respuesta;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FichaService {

    private static final String ARCHIVO_FICHAS = "fichas.json";
    private static final String ARCHIVO_HISTORIAL = "historial.json";
    private static final ZoneId ZONA_CR = ZoneId.of("America/Costa_Rica");
    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static FichaService instancia;

    private static final char[] LETRAS = {'A', 'B', 'C', 'D', 'E'};

    private static final int NUMEROS_POR_LETRA = 10;

    private static final int CICLO = LETRAS.length * NUMEROS_POR_LETRA;

    public static FichaService getInstancia() {
        if (instancia == null) {
            instancia = new FichaService();
        }
        return instancia;
    }

    public Respuesta generarFicha(String tramiteId, String sucursalId,
            String cedulaCliente, boolean preferencial) {
        try {
            List<Ficha> fichasActivas = GsonUtil.leerLista(ARCHIVO_FICHAS, Ficha.class);
            List<Ficha> fichasDeSucursal = fichasActivas.stream()
                    .filter(f -> sucursalId != null && sucursalId.equals(f.getSucursalId()))
                    .collect(Collectors.toList());

            if (!fichasDeSucursal.isEmpty() && esDeOtroDia(fichasDeSucursal.get(0))) {
                Respuesta rArchivo = archivarFichas(fichasActivas);
                if (!rArchivo.getEstado()) {
                    return rArchivo;
                }
                fichasActivas = new ArrayList<>();
            }

            int[] posicion = calcularPosicionGlobal(fichasDeSucursal);
            String letra = String.valueOf(LETRAS[posicion[0]]);
            int numero = posicion[1];

            String fechaHoy = LocalDate.now(ZONA_CR).format(FORMATO_FECHA);
            String id = letra + "-" + String.format("%03d", numero) + "-" + fechaHoy + "-" + System.currentTimeMillis();

            Ficha ficha = new Ficha(id, numero, letra, tramiteId, sucursalId, cedulaCliente, preferencial);

            fichasActivas.add(ficha);
            GsonUtil.guardar(fichasActivas, ARCHIVO_FICHAS);

            return new Respuesta(true, "Ficha generada: " + ficha.getCodigo(), "", "ficha", ficha);

        } catch (Exception e) {
            return new Respuesta(false,
                    "No se pudo generar la ficha.",
                    "FichaService.generarFicha > " + e.getMessage());
        }
    }

    /**
     * Devuelve TODAS las fichas activas del archivo (sin filtrar por sucursal).
     * Usar solo cuando se necesita acceso global (ej: archivado, proyección general).
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
     * Devuelve solo las fichas activas pertenecientes a la sucursal indicada.
     * Usar en la VentanaFuncionario y cualquier vista que deba operar
     * exclusivamente sobre la sucursal configurada en la sesión actual.
     *
     * @param sucursalId ID de la sucursal a filtrar; si es null devuelve lista vacía.
     */
    public Respuesta obtenerFichasActivasPorSucursal(String sucursalId) {
        try {
            if (sucursalId == null || sucursalId.isBlank()) {
                return new Respuesta(true, "", "", "lista", new ArrayList<>());
            }

            List<Ficha> todas = GsonUtil.leerLista(ARCHIVO_FICHAS, Ficha.class);
            List<Ficha> deSucursal = todas.stream()
                    .filter(f -> sucursalId.equals(f.getSucursalId()))
                    .collect(Collectors.toList());

            return new Respuesta(true, "", "", "lista", deSucursal);
        } catch (Exception e) {
            return new Respuesta(false,
                    "No se pudo obtener las fichas de la sucursal.",
                    "FichaService.obtenerFichasActivasPorSucursal > " + e.getMessage());
        }
    }

    public Respuesta obtenerUltimasDelHistorial(int cantidad) {
        try {
            List<Ficha> historial = GsonUtil.leerLista(ARCHIVO_HISTORIAL, Ficha.class);

            int desde = Math.max(0, historial.size() - cantidad);
            List<Ficha> ultimas = historial.subList(desde, historial.size());

            return new Respuesta(true, "", "", "lista", new ArrayList<>(ultimas));
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo obtener el historial.", "FichaService.obtenerUltimasDelHistorial > " + e.getMessage());
        }
    }

    public Respuesta actualizarEstado(String fichaId, Ficha.Estado estado) {
        try {
            List<Ficha> fichas = GsonUtil.leerLista(ARCHIVO_FICHAS, Ficha.class);

            Ficha encontrada = fichas.stream().filter(f -> f.getId().equals(fichaId)).findFirst().orElse(null);

            if (encontrada == null) {
                return new Respuesta(false, "Ficha no encontrada.", "");
            }

            encontrada.setEstado(estado);
            GsonUtil.guardar(fichas, ARCHIVO_FICHAS);

            return new Respuesta(true, "Estado actualizado.", "", "ficha", encontrada);

        } catch (Exception e) {
            return new Respuesta(false, "No se pudo actualizar la ficha.", "FichaService.actualizarEstado > " + e.getMessage());
        }
    }

    private int[] calcularPosicionGlobal(List<Ficha> fichasActivas) {
        int indiceGlobal = fichasActivas.size() % CICLO;
        int indiceLetra = indiceGlobal / NUMEROS_POR_LETRA;
        int numero = (indiceGlobal % NUMEROS_POR_LETRA) + 1;
        return new int[]{indiceLetra, numero};
    }

    private boolean esDeOtroDia(Ficha ficha) {
        String fechaHoy = LocalDate.now(ZONA_CR).format(FORMATO_FECHA);

        String fechaFicha = ficha.getFechaHoraEmision().substring(0, 10);
        return !fechaFicha.equals(fechaHoy);
    }

    private Respuesta archivarFichas(List<Ficha> fichasAArchivar) {
        try {
            List<Ficha> historialExistente = GsonUtil.leerLista(ARCHIVO_HISTORIAL, Ficha.class);
            historialExistente.addAll(fichasAArchivar);
            GsonUtil.guardar(historialExistente, ARCHIVO_HISTORIAL);
            GsonUtil.guardar(new ArrayList<>(), ARCHIVO_FICHAS);
            System.out.println("[FichaService] " + fichasAArchivar.size() + " fichas archivadas en historial.json");
            return new Respuesta(true, "", "");
        } catch (Exception e) {
            return new Respuesta(false, "Error al archivar fichas.", "FichaService.archivarFichas > " + e.getMessage());
        }
    }

    public String getNombreTramite(Ficha ficha) {
        if (ficha == null || ficha.getTramiteId() == null) {
            return "Sin trámite";
        }

        Tramite tramite = TramiteService.getInstancia().buscarPorId(ficha.getTramiteId());
        return tramite != null ? tramite.getNombre() : "Trámite no encontrado";
    }

    public String getCodigoLetra(Ficha ficha) {
        if (ficha == null || ficha.getCodigoLetra() == null) {
            return "-";
        }
        return ficha.getCodigoLetra();
    }

    public Respuesta registrarLlamado(String fichaId, String estacionId) {
        try {
            List<Ficha> fichas = GsonUtil.leerLista(ARCHIVO_FICHAS, Ficha.class);

            Ficha encontrada = fichas.stream().filter(f -> f.getId().equals(fichaId)).findFirst().orElse(null);

            if (encontrada == null) {
                return new Respuesta(false, "Ficha no encontrada.", "");
            }

            encontrada.registrarLlamado(estacionId);
            GsonUtil.guardar(fichas, ARCHIVO_FICHAS);

            return new Respuesta(true, "Llamado registrado.", "", "ficha", encontrada);

        } catch (Exception e) {
            return new Respuesta(false, "No se pudo registrar el llamado.", "FichaService.registrarLlamado > " + e.getMessage());
        }
    }
}