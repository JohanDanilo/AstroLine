package cr.ac.una.astroline.service;
import cr.ac.una.astroline.model.ConfiguracionLocal;
import cr.ac.una.astroline.util.GsonUtil;
import cr.ac.una.astroline.util.Respuesta;

/**
 * Servicio singleton para leer y escribir la configuración local del equipo.
 * A diferencia de otros services, no maneja listas — es un objeto único.
 *
 * @author JohanDanilo
 */
public class ConfiguracionService {

    private static final String ARCHIVO_JSON = "configuracion.json";
    private static ConfiguracionService instancia;
    private ConfiguracionLocal configuracion;

    private ConfiguracionService() {
    }

    public static ConfiguracionService getInstancia() {
        if (instancia == null) {
            instancia = new ConfiguracionService();
            instancia.cargarConfiguracion();
        }
        return instancia;
    }

    public ConfiguracionLocal getConfiguracion() {
        return configuracion;
    }

    /**
     * Retorna el sucursalId configurado para esta máquina.
     * Si no hay configuración, retorna "sucursal-1" como fallback seguro.
     */
    public String getSucursalId() {
        if (configuracion == null || configuracion.getSucursalId() == null)
            return "sucursal-1";
        return configuracion.getSucursalId();
    }

    /**
     * Retorna el estacionId configurado para esta máquina.
     * Null si opera como Kiosko.
     */
    public String getEstacionId() {
        if (configuracion == null) return null;
        return configuracion.getEstacionId();
    }

    /**
     * Guarda una nueva configuración local.
     *
     * @param sucursalId id de la sucursal
     * @param estacionId id de la estación, null si es Kiosko
     * @return Respuesta con estado del resultado
     */
    public Respuesta guardarConfiguracion(String sucursalId, String estacionId) {
        try {
            if (sucursalId == null || sucursalId.isBlank())
                return new Respuesta(false, "El id de sucursal no puede estar vacío.", "");

            configuracion = new ConfiguracionLocal(sucursalId, estacionId);
            GsonUtil.guardar(configuracion, ARCHIVO_JSON);
            return new Respuesta(true, "Configuración guardada.", "",
                    "configuracion", configuracion);
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo guardar la configuración.",
                    "ConfiguracionService.guardarConfiguracion > " + e.getMessage());
        }
    }

    private void cargarConfiguracion() {
        configuracion = GsonUtil.leer(ARCHIVO_JSON, ConfiguracionLocal.class);
        // Si no existe el archivo, configuracion queda null
        // getSucursalId() maneja ese caso con fallback
    }
    

    /**
     * Fuerza la relectura de configuracion.json desde disco.
     */
    public void recargarConfiguracion() {
        configuracion = GsonUtil.leer(ARCHIVO_JSON, ConfiguracionLocal.class);
    }
}