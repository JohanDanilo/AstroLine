package cr.ac.una.astroline.model;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa una ficha de atención asignada a un cliente.
 * Es el objeto central del sistema — todos los módulos lo usan.
 * Se persiste en data/fichas.json
 *
 * El código visible (ej: A-001) se determina por posición global,
 * NO por el trámite elegido. Las primeras 10 fichas del día son A-001
 * a A-010, las siguientes B-001 a B-010, y así hasta E-010 (50 fichas),
 * luego se reinicia en A-001.
 *
 * @author JohanDanilo
 */
public class Ficha {

    /**
     * Estados posibles de una ficha durante su ciclo de vida.
     */
    public enum Estado {
        ESPERANDO,  // asignada en el Kiosko, esperando ser llamada
        LLAMADA,    // el funcionario la llamó
        //ATENDIDA,   // ya fue atendida
        AUSENTE     // fue llamada pero el cliente no se presentó
    }

    private String id;
    private int numero;
    private String codigoLetra;      // letra asignada por posición global (A-E)
    private String tramiteId;        // trámite que el cliente eligió (se guarda, no determina la letra)
    private String sucursalId;
    private String estacionId;       // se asigna cuando el funcionario la llama
    private String cedulaCliente;    // null si el cliente no se identificó
    private boolean preferencial;
    private Estado estado;
    private String fechaHoraEmision; // momento en que se generó en el Kiosko
    private String fechaHoraLlamado; // momento en que el funcionario la llamó

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final ZoneId ZONA_CR = ZoneId.of("America/Costa_Rica");

    public Ficha() {
        this.estado = Estado.ESPERANDO;
    }

    public Ficha(String id, int numero, String codigoLetra, String tramiteId,
            String sucursalId, String cedulaCliente, boolean preferencial) {
        this.id = id;
        this.numero = numero;
        this.codigoLetra = codigoLetra;
        this.tramiteId = tramiteId;
        this.sucursalId = sucursalId;
        this.cedulaCliente = cedulaCliente;
        this.preferencial = preferencial;
        this.estado = Estado.ESPERANDO;
        this.fechaHoraEmision = ZonedDateTime.now(ZONA_CR).format(FORMATTER);
    }

    /**
     * Registra el momento exacto en que el funcionario llamó esta ficha.
     * Cambia el estado a LLAMADA automáticamente.
     */
    public void registrarLlamado(String estacionId) {
        this.estacionId = estacionId;
        this.estado = Estado.LLAMADA;
        this.fechaHoraLlamado = ZonedDateTime.now(ZONA_CR).format(FORMATTER);
    }

    /**
     * Retorna el número de ficha formateado con ceros: 001, 023, 100.
     * Es lo que se muestra en la pantalla de Proyección y en el PDF.
     *
     * @return número formateado con 3 dígitos
     */
    public String getNumeroFormateado() {
        return String.format("%03d", numero);
    }

    /**
     * Indica si la ficha está pendiente de atención.
     *
     * @return true si el estado es ESPERANDO
     */
    public boolean estaEsperando() {
        return estado == Estado.ESPERANDO;
    }

    /**
     * Retorna el código visible de la ficha: letra por posición global + número.
     * Ejemplo: "A-001", "B-005", "E-010"
     * La letra NO proviene del trámite elegido, sino de la posición global del día.
     *
     * @return código legible de la ficha
     */
    public String getCodigo() {
        return codigoLetra + "-" + getNumeroFormateado();
    }

    // ── Getters y Setters ─────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public String getCodigoLetra() { return codigoLetra; }
    public void setCodigoLetra(String codigoLetra) { this.codigoLetra = codigoLetra; }

    public String getTramiteId() { return tramiteId; }
    public void setTramiteId(String tramiteId) { this.tramiteId = tramiteId; }

    public String getSucursalId() { return sucursalId; }
    public void setSucursalId(String sucursalId) { this.sucursalId = sucursalId; }

    public String getEstacionId() { return estacionId; }
    public void setEstacionId(String estacionId) { this.estacionId = estacionId; }

    public String getCedulaCliente() { return cedulaCliente; }
    public void setCedulaCliente(String cedulaCliente) { this.cedulaCliente = cedulaCliente; }

    public boolean isPreferencial() { return preferencial; }
    public void setPreferencial(boolean preferencial) { this.preferencial = preferencial; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public String getFechaHoraEmision() { return fechaHoraEmision; }
    public void setFechaHoraEmision(String fechaHoraEmision) { this.fechaHoraEmision = fechaHoraEmision; }

    public String getFechaHoraLlamado() { return fechaHoraLlamado; }
    public void setFechaHoraLlamado(String fechaHoraLlamado) { this.fechaHoraLlamado = fechaHoraLlamado; }
    
    @Override
    public String toString() {
        return "Ficha{codigo=" + getCodigo() +
                ", tramite='" + tramiteId +
                "', preferencial=" + preferencial +
                ", estado=" + estado + "}";
    }
}