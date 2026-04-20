package cr.ac.una.astroline.model;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Ficha {

    public enum Estado {
        ESPERANDO,
        LLAMADA,
        ATENDIDA,
        AUSENTE
    }

    private String id;
    private int numero;
    private String codigoLetra;
    private String tramiteId;
    private String sucursalId;
    private String estacionId;
    private String cedulaCliente;
    private boolean preferencial;
    private Estado estado;
    private String fechaHoraEmision;
    private String fechaHoraLlamado;

    private static final DateTimeFormatter FORMATTER
            = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
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

    public void registrarLlamado(String estacionId) {
        this.estacionId = estacionId;
        this.estado = Estado.LLAMADA;
        this.fechaHoraLlamado = ZonedDateTime.now(ZONA_CR).format(FORMATTER);
    }

    public String getNumeroFormateado() {
        return String.format("%03d", numero);
    }

    public boolean estaEsperando() {
        return estado == Estado.ESPERANDO;
    }

    public String getCodigo() {
        return codigoLetra + "-" + getNumeroFormateado();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getCodigoLetra() {
        return codigoLetra;
    }

    public void setCodigoLetra(String codigoLetra) {
        this.codigoLetra = codigoLetra;
    }

    public String getTramiteId() {
        return tramiteId;
    }

    public void setTramiteId(String tramiteId) {
        this.tramiteId = tramiteId;
    }

    public String getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(String sucursalId) {
        this.sucursalId = sucursalId;
    }

    public String getEstacionId() {
        return estacionId;
    }

    public void setEstacionId(String estacionId) {
        this.estacionId = estacionId;
    }

    public String getCedulaCliente() {
        return cedulaCliente;
    }

    public void setCedulaCliente(String cedulaCliente) {
        this.cedulaCliente = cedulaCliente;
    }

    public boolean isPreferencial() {
        return preferencial;
    }

    public void setPreferencial(boolean preferencial) {
        this.preferencial = preferencial;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public String getFechaHoraEmision() {
        return fechaHoraEmision;
    }

    public void setFechaHoraEmision(String fechaHoraEmision) {
        this.fechaHoraEmision = fechaHoraEmision;
    }

    public String getFechaHoraLlamado() {
        return fechaHoraLlamado;
    }

    public void setFechaHoraLlamado(String fechaHoraLlamado) {
        this.fechaHoraLlamado = fechaHoraLlamado;
    }

    @Override
    public String toString() {
        return "Ficha{codigo=" + getCodigo() + ", tramite='" + tramiteId + "', preferencial=" + preferencial + ", estado=" + estado + "}";
    }
}
