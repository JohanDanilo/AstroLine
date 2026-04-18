package cr.ac.una.astroline.service;

import cr.ac.una.astroline.model.Cliente;
import cr.ac.una.astroline.model.Ficha;
import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.model.Tramite;
import cr.ac.una.astroline.util.GsonUtil;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Servicio de solo lectura para construir estadísticas reales a partir de los
 * archivos JSON del sistema.
 */
public class EstadisticasService {

    private static final String ARCHIVO_CLIENTES = "clientes.json";
    private static final String ARCHIVO_FICHAS = "fichas.json";
    private static final String ARCHIVO_HISTORIAL = "historial.json";
    private static final String ARCHIVO_SUCURSALES = "sucursales.json";
    private static final String ARCHIVO_TRAMITES = "tramites.json";

    private static final ZoneId ZONA_CR = ZoneId.of("America/Costa_Rica");
    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final int LIMITE_RANKING = 5;

    public EstadisticasResumen obtenerResumen(String periodoTexto, String sucursalId) {
        PeriodoEstadistico periodo = PeriodoEstadistico.fromLabel(periodoTexto);
        LocalDate hoy = LocalDate.now(ZONA_CR);

        Map<String, Cliente> clientesPorCedula = cargarClientesPorCedula();
        Map<String, Tramite> tramitesPorId = cargarTramitesPorId();
        List<Ficha> fichasFiltradas = cargarFichasFiltradas(periodo, sucursalId, hoy);

        Map<String, Long> distribucionTramites =
                construirDistribucionTramites(fichasFiltradas, tramitesPorId);

        return new EstadisticasResumen(
                periodo,
                calcularTotalClientes(fichasFiltradas),
                fichasFiltradas.size(),
                construirSeriePorDia(fichasFiltradas, periodo, hoy),
                distribucionTramites,
                construirTopClientes(fichasFiltradas, clientesPorCedula),
                construirTopTramites(distribucionTramites)
        );
    }

    public List<SucursalOpcion> obtenerSucursalesDisponibles() {
        List<Sucursal> sucursales = GsonUtil.leerLista(ARCHIVO_SUCURSALES, Sucursal.class);
        sucursales.sort(Comparator.comparing(
                Sucursal::getNombre,
                String.CASE_INSENSITIVE_ORDER
        ));

        List<SucursalOpcion> opciones = new ArrayList<>();
        opciones.add(new SucursalOpcion(null, "Todas las sucursales"));

        for (Sucursal sucursal : sucursales) {
            opciones.add(new SucursalOpcion(sucursal.getId(), sucursal.getNombre()));
        }

        return Collections.unmodifiableList(opciones);
    }

    private List<Ficha> cargarFichasFiltradas(PeriodoEstadistico periodo,
            String sucursalId, LocalDate hoy) {
        LocalDate inicio = periodo.calcularFechaInicio(hoy);
        List<Ficha> resultado = new ArrayList<>();

        for (Ficha ficha : cargarTodasLasFichas()) {
            if (ficha == null) {
                continue;
            }
            if (sucursalId != null && !Objects.equals(sucursalId, ficha.getSucursalId())) {
                continue;
            }

            LocalDate fechaEmision = obtenerFechaEmision(ficha);
            if (fechaEmision == null) {
                continue;
            }
            if (fechaEmision.isBefore(inicio) || fechaEmision.isAfter(hoy)) {
                continue;
            }

            resultado.add(ficha);
        }

        return resultado;
    }

    private List<Ficha> cargarTodasLasFichas() {
        List<Ficha> fichas = new ArrayList<>();
        fichas.addAll(GsonUtil.leerLista(ARCHIVO_HISTORIAL, Ficha.class));
        fichas.addAll(GsonUtil.leerLista(ARCHIVO_FICHAS, Ficha.class));
        return fichas;
    }

    private Map<String, Cliente> cargarClientesPorCedula() {
        Map<String, Cliente> mapa = new LinkedHashMap<>();
        for (Cliente cliente : GsonUtil.leerLista(ARCHIVO_CLIENTES, Cliente.class)) {
            if (cliente != null && cliente.getCedula() != null) {
                mapa.put(cliente.getCedula(), cliente);
            }
        }
        return mapa;
    }

    private Map<String, Tramite> cargarTramitesPorId() {
        Map<String, Tramite> mapa = new LinkedHashMap<>();
        for (Tramite tramite : GsonUtil.leerLista(ARCHIVO_TRAMITES, Tramite.class)) {
            if (tramite != null && tramite.getId() != null) {
                mapa.put(tramite.getId(), tramite);
            }
        }
        return mapa;
    }

    private int calcularTotalClientes(List<Ficha> fichas) {
        LinkedHashSet<String> clientesIdentificados = new LinkedHashSet<>();
        int clientesAnonimos = 0;

        for (Ficha ficha : fichas) {
            String cedula = ficha.getCedulaCliente();
            if (cedula == null || cedula.isBlank()) {
                clientesAnonimos++;
            } else {
                clientesIdentificados.add(cedula);
            }
        }

        return clientesIdentificados.size() + clientesAnonimos;
    }

    private Map<LocalDate, Long> construirSeriePorDia(List<Ficha> fichas,
            PeriodoEstadistico periodo, LocalDate hoy) {
        LinkedHashMap<LocalDate, Long> serie = new LinkedHashMap<>();

        for (LocalDate fecha : periodo.construirRango(hoy)) {
            serie.put(fecha, 0L);
        }

        for (Ficha ficha : fichas) {
            LocalDate fecha = obtenerFechaEmision(ficha);
            if (fecha != null && serie.containsKey(fecha)) {
                serie.merge(fecha, 1L, Long::sum);
            }
        }

        return Collections.unmodifiableMap(serie);
    }

    private Map<String, Long> construirDistribucionTramites(List<Ficha> fichas,
            Map<String, Tramite> tramitesPorId) {
        Map<String, Long> conteo = new LinkedHashMap<>();

        for (Ficha ficha : fichas) {
            String nombreTramite = resolverNombreTramite(ficha.getTramiteId(), tramitesPorId);
            conteo.merge(nombreTramite, 1L, Long::sum);
        }

        return ordenarPorCantidadDesc(conteo);
    }

    private List<RankingItem> construirTopClientes(List<Ficha> fichas,
            Map<String, Cliente> clientesPorCedula) {
        Map<String, Long> conteo = new LinkedHashMap<>();

        for (Ficha ficha : fichas) {
            String cedula = ficha.getCedulaCliente();
            if (cedula == null || cedula.isBlank()) {
                continue;
            }
            conteo.merge(cedula, 1L, Long::sum);
        }

        return construirRanking(conteo, entry ->
                resolverNombreCliente(entry.getKey(), clientesPorCedula), LIMITE_RANKING);
    }

    private List<RankingItem> construirTopTramites(Map<String, Long> distribucionTramites) {
        List<RankingItem> ranking = new ArrayList<>();
        int agregados = 0;

        for (Map.Entry<String, Long> entry : distribucionTramites.entrySet()) {
            ranking.add(new RankingItem(entry.getKey(), entry.getValue()));
            agregados++;
            if (agregados >= LIMITE_RANKING) {
                break;
            }
        }

        return Collections.unmodifiableList(ranking);
    }

    private List<RankingItem> construirRanking(Map<String, Long> conteo,
            java.util.function.Function<Map.Entry<String, Long>, String> resolvedorNombre,
            int limite) {
        List<Map.Entry<String, Long>> entradas = new ArrayList<>(conteo.entrySet());
        entradas.sort((a, b) -> {
            int porCantidad = Long.compare(b.getValue(), a.getValue());
            if (porCantidad != 0) {
                return porCantidad;
            }
            return a.getKey().compareToIgnoreCase(b.getKey());
        });

        List<RankingItem> ranking = new ArrayList<>();
        for (int i = 0; i < entradas.size() && i < limite; i++) {
            Map.Entry<String, Long> entry = entradas.get(i);
            ranking.add(new RankingItem(resolvedorNombre.apply(entry), entry.getValue()));
        }

        return Collections.unmodifiableList(ranking);
    }

    private Map<String, Long> ordenarPorCantidadDesc(Map<String, Long> conteo) {
        List<Map.Entry<String, Long>> entradas = new ArrayList<>(conteo.entrySet());
        entradas.sort((a, b) -> {
            int porCantidad = Long.compare(b.getValue(), a.getValue());
            if (porCantidad != 0) {
                return porCantidad;
            }
            return a.getKey().compareToIgnoreCase(b.getKey());
        });

        LinkedHashMap<String, Long> ordenado = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : entradas) {
            ordenado.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(ordenado);
    }

    private String resolverNombreCliente(String cedula, Map<String, Cliente> clientesPorCedula) {
        Cliente cliente = clientesPorCedula.get(cedula);
        if (cliente != null) {
            String nombreCompleto = cliente.getNombreCompleto();
            if (nombreCompleto != null && !nombreCompleto.isBlank()) {
                return nombreCompleto;
            }
        }
        return "Cliente " + cedula;
    }

    private String resolverNombreTramite(String tramiteId, Map<String, Tramite> tramitesPorId) {
        Tramite tramite = tramitesPorId.get(tramiteId);
        if (tramite != null && tramite.getNombre() != null && !tramite.getNombre().isBlank()) {
            return tramite.getNombre();
        }
        if (tramiteId == null || tramiteId.isBlank()) {
            return "Trámite sin definir";
        }
        return "Trámite " + tramiteId;
    }

    private LocalDate obtenerFechaEmision(Ficha ficha) {
        LocalDateTime fechaHora = parseFechaHora(ficha.getFechaHoraEmision());
        return fechaHora != null ? fechaHora.toLocalDate() : null;
    }

    private LocalDateTime parseFechaHora(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(texto, FORMATO_FECHA_HORA);
        } catch (Exception ex) {
            return null;
        }
    }

    public enum PeriodoEstadistico {
        HOY("Hoy", "Clientes hoy", "Trámites hoy") {
            @Override
            LocalDate calcularFechaInicio(LocalDate hoy) {
                return hoy;
            }
        },
        SEMANA("Semana", "Clientes esta semana", "Trámites esta semana") {
            @Override
            LocalDate calcularFechaInicio(LocalDate hoy) {
                return hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            }
        },
        MES("Mes", "Clientes este mes", "Trámites este mes") {
            @Override
            LocalDate calcularFechaInicio(LocalDate hoy) {
                return hoy.withDayOfMonth(1);
            }
        };

        private final String etiquetaFiltro;
        private final String tituloClientes;
        private final String tituloTramites;

        PeriodoEstadistico(String etiquetaFiltro, String tituloClientes, String tituloTramites) {
            this.etiquetaFiltro = etiquetaFiltro;
            this.tituloClientes = tituloClientes;
            this.tituloTramites = tituloTramites;
        }

        abstract LocalDate calcularFechaInicio(LocalDate hoy);

        public List<LocalDate> construirRango(LocalDate hoy) {
            LocalDate inicio = calcularFechaInicio(hoy);
            List<LocalDate> fechas = new ArrayList<>();
            for (LocalDate fecha = inicio; !fecha.isAfter(hoy); fecha = fecha.plusDays(1)) {
                fechas.add(fecha);
            }
            return fechas;
        }

        public String getEtiquetaFiltro() {
            return etiquetaFiltro;
        }

        public String getTituloClientes() {
            return tituloClientes;
        }

        public String getTituloTramites() {
            return tituloTramites;
        }

        public static PeriodoEstadistico fromLabel(String label) {
            for (PeriodoEstadistico periodo : values()) {
                if (periodo.etiquetaFiltro.equalsIgnoreCase(label)) {
                    return periodo;
                }
            }
            return HOY;
        }
    }

    public static class EstadisticasResumen {

        private final PeriodoEstadistico periodo;
        private final int totalClientes;
        private final int totalTramites;
        
        private final Map<LocalDate, Long> tramitesPorDia;
        private final Map<String, Long> distribucionTramites;
        private final List<RankingItem> topClientes;
        private final List<RankingItem> topTramites;

        public EstadisticasResumen(PeriodoEstadistico periodo, int totalClientes,
                int totalTramites,
                Map<LocalDate, Long> tramitesPorDia,
                Map<String, Long> distribucionTramites,
                List<RankingItem> topClientes,
                List<RankingItem> topTramites) {
            this.periodo = periodo;
            this.totalClientes = totalClientes;
            this.totalTramites = totalTramites;
            this.tramitesPorDia = tramitesPorDia;
            this.distribucionTramites = distribucionTramites;
            this.topClientes = topClientes;
            this.topTramites = topTramites;
        }

        public PeriodoEstadistico getPeriodo() {
            return periodo;
        }

        public int getTotalClientes() {
            return totalClientes;
        }

        public int getTotalTramites() {
            return totalTramites;
        }

        public Map<LocalDate, Long> getTramitesPorDia() {
            return tramitesPorDia;
        }

        public Map<String, Long> getDistribucionTramites() {
            return distribucionTramites;
        }

        public List<RankingItem> getTopClientes() {
            return topClientes;
        }

        public List<RankingItem> getTopTramites() {
            return topTramites;
        }
    }

    public static class RankingItem {

        private final String nombre;
        private final long total;

        public RankingItem(String nombre, long total) {
            this.nombre = nombre;
            this.total = total;
        }

        public String getNombre() {
            return nombre;
        }

        public long getTotal() {
            return total;
        }
    }

    public static class SucursalOpcion {

        private final String id;
        private final String nombre;

        public SucursalOpcion(String id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public String getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }
    }
}
