package cr.ac.una.astroline.model;

/**
 * Configuración local de la máquina donde corre el sistema.
 * Define en qué sucursal y estación opera este equipo.
 * Se persiste en data/configuracion.json
 * Cada PC del sistema tiene su propio archivo independiente.
 *
 * @author JohanDanilo
 */
public class ConfiguracionLocal {

    private String sucursalId;
    private String estacionId; // null si esta máquina es un Kiosko o la pantalla de proyeccion

    public ConfiguracionLocal() {
    }

    public ConfiguracionLocal(String sucursalId, String estacionId) {
        this.sucursalId = sucursalId;
        this.estacionId = estacionId;
    }

    /**
     * Indica si esta máquina está configurada como estación de funcionario.
     * Si estacionId es null, puede operar como Kiosko o como la panatalla de proyeccion.
     */
    public boolean esEstacionFuncionario() {
        return estacionId != null && !estacionId.isBlank();
    }

    public String getSucursalId() { return sucursalId; }
    public void setSucursalId(String sucursalId) { this.sucursalId = sucursalId; }

    public String getEstacionId() { return estacionId; }
    public void setEstacionId(String estacionId) { this.estacionId = estacionId; }

    @Override
    public String toString() {
        return "ConfiguracionLocal{sucursalId='" + sucursalId +
                "', estacionId='" + estacionId + "'}";
    }
}