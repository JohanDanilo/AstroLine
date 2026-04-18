package cr.ac.una.astroline.service;

import cr.ac.una.astroline.model.Ficha;
import cr.ac.una.astroline.util.DataNotifier;
import cr.ac.una.astroline.util.GsonUtil;
import cr.ac.una.astroline.util.Respuesta;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;

/**
 * Lógica de negocio para la gestión de fichas.
 * Maneja generación, persistencia y archivado automático al historial.
 *
 * Sistema de códigos de ficha:
 *   - El código visible (A-001, B-005...) se calcula por posición global del día.
 *   - Las fichas 1-10  → A-001 a A-010
 *   - Las fichas 11-20 → B-001 a B-010
 *   - Las fichas 21-30 → C-001 a C-010
 *   - Las fichas 31-40 → D-001 a D-010
 *   - Las fichas 41-50 → E-001 a E-010
 *   - La ficha 51 reinicia en A-001 (ciclo de 50)
 *   El trámite elegido por el cliente se guarda en tramiteId pero NO
 *   determina la letra del código.
 *
 * @author AstroLine
 */
public class FichaService implements DataNotifier.Listener{

    private static FichaService instancia;
    
    private static final String ARCHIVO_FICHAS    = "fichas.json";
    private static final String ARCHIVO_HISTORIAL = "historial.json";
    private static final ZoneId ZONA_CR           = ZoneId.of("America/Costa_Rica");
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /** Letras disponibles en orden. Deben ser exactamente 5. */
    private static final char[] LETRAS = {'A', 'B', 'C', 'D', 'E'};

    /** Números por letra (del 1 al 10). */
    private static final int NUMEROS_POR_LETRA = 10;

    /** Tamaño total del ciclo: 5 letras × 10 números = 50 fichas. */
    private static final int CICLO = LETRAS.length * NUMEROS_POR_LETRA;

    private FichaService() {DataNotifier.subscribe(this);}
    
    public static FichaService getInstancia(){
        if(instancia == null)
            instancia = new FichaService();
        return instancia;
       
    }
    

    // -------------------------------------------------------------------------
    // GENERACIÓN DE FICHA
    // -------------------------------------------------------------------------

    /**
     * Genera una nueva ficha para el trámite indicado.
     * Si es la primera ficha del día, archiva las fichas anteriores primero.
     *
     * El código visible (letra + número) se determina por la posición global
     * de la ficha en el día, independientemente del trámite elegido.
     *
     * @param tramiteId      id del trámite (guardado para registro, no afecta el código)
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

            // Calcular letra y número por posición global en el ciclo
            int[] posicion = calcularPosicionGlobal(fichasActivas);
            String letra   = String.valueOf(LETRAS[posicion[0]]);
            int numero     = posicion[1];

            // ID único: letra + número + fecha + timestamp (evita colisiones al reiniciar)
            String fechaHoy = LocalDate.now(ZONA_CR).format(FORMATO_FECHA);
            String id = letra + "-" + String.format("%03d", numero)
                      + "-" + fechaHoy + "-" + System.currentTimeMillis();

            Ficha ficha = new Ficha(id, numero, letra, tramiteId,
                    sucursalId, cedulaCliente, preferencial);

            fichasActivas.add(ficha);
            GsonUtil.guardarYPropagar(fichasActivas, ARCHIVO_FICHAS);
            
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
    public Respuesta actualizarEstado(String fichaId, Ficha.Estado estado, String estacionId) {
        try {
            List<Ficha> fichas = GsonUtil.leerLista(ARCHIVO_FICHAS, Ficha.class);

            Ficha encontrada = fichas.stream()
                    .filter(f -> f.getId().equals(fichaId))
                    .findFirst()
                    .orElse(null);

            if (encontrada == null) 
                return new Respuesta(false, "Ficha no encontrada.", "");
            
            if(estado == Ficha.Estado.LLAMADA && estacionId != null)
                encontrada.registrarLlamado(estacionId);
            
            encontrada.setEstado(estado);
            GsonUtil.guardarYPropagar(fichas, ARCHIVO_FICHAS);
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
     * Calcula la letra y el número de la próxima ficha según la posición
     * global dentro del ciclo de 50 (5 letras × 10 números).
     *
     * Ejemplos:
     *   0 fichas activas → índice 0  → A-001
     *   9 fichas activas → índice 9  → A-010
     *  10 fichas activas → índice 10 → B-001
     *  49 fichas activas → índice 49 → E-010
     *  50 fichas activas → índice 0  → A-001 (reinicio)
     *
     * @param fichasActivas lista actual de fichas del día
     * @return int[0] = índice de letra (0=A, 1=B...), int[1] = número (1-10)
     */
    private int[] calcularPosicionGlobal(List<Ficha> fichasActivas) {
        int indiceGlobal = fichasActivas.size() % CICLO; // reinicia cada 50
        int indiceLetra  = indiceGlobal / NUMEROS_POR_LETRA; // 0-4
        int numero       = (indiceGlobal % NUMEROS_POR_LETRA) + 1; // 1-10
        return new int[]{indiceLetra, numero};
    }

    /**
     * Verifica si una ficha fue emitida en un día diferente al de hoy.
     */
    private boolean esDeOtroDia(Ficha ficha) {
        String fechaHoy  = LocalDate.now(ZONA_CR).format(FORMATO_FECHA);
        // fechaHoraEmision tiene formato "dd-MM-yyyy HH:mm:ss"
        // los primeros 10 caracteres son la fecha
        String fechaFicha = ficha.getFechaHoraEmision().substring(0, 10);
        return !fechaFicha.equals(fechaHoy);
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
    
    
    //METODO PARA PROYECCION 

    public List<Ficha> obtenerFichasParaProyeccion(String sucursalId, boolean esOrdenDesendente ,int nFichas){
        
        List<Ficha> listaActual = (List<Ficha>)obtenerFichasActivas().getResultado("lista");
        if(listaActual == null || listaActual.isEmpty()) return new ArrayList<>();
        List<Ficha> listaAtendidas = new ArrayList<>();
        
        for(Ficha actual : listaActual)
            if((actual.getEstado() == Ficha.Estado.ATENDIDA 
                    || actual.getEstado() == Ficha.Estado.LLAMADA) 
                    && actual.getSucursalId().equals(sucursalId))
                listaAtendidas.add(actual);
        
         listaAtendidas.sort((ficha1, ficha2) -> {
          if(esOrdenDesendente){
            if(ficha1.getFechaHoraLlamado() == null) return 1;
            if(ficha2.getFechaHoraLlamado() == null) return -1; 
          }
          else {
            if(ficha1.getFechaHoraLlamado() == null) return -1;
            if(ficha2.getFechaHoraLlamado() == null) return 1; 
          }
          DateTimeFormatter formatoParaComparar = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
          
          ZonedDateTime timeFicha1 = LocalDateTime.parse(ficha1.getFechaHoraLlamado(),formatoParaComparar).atZone(ZONA_CR);
          ZonedDateTime timeFicha2 = LocalDateTime.parse(ficha2.getFechaHoraLlamado(),formatoParaComparar).atZone(ZONA_CR);
          
          return timeFicha2.compareTo(timeFicha1);
          
        });
        
        List<Ficha> ordenada = new ArrayList<>();
        
        for (int i = 0; i < listaAtendidas.size() && i < nFichas; i++)
            ordenada.add(listaAtendidas.get(i));
        
        return ordenada;
    }

    @Override
    public void onDataChanged(String fileName) {
        if(!fileName.equals(ARCHIVO_FICHAS)) return;
        
        System.out.println("[FichaService] Detectado cambio externo, sincronizando...");
    }
}