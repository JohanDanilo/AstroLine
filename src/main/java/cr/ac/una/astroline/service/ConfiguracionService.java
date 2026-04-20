package cr.ac.una.astroline.service;

import cr.ac.una.astroline.model.ConfiguracionLocal;
import cr.ac.una.astroline.model.Tramite;
import cr.ac.una.astroline.util.GsonUtil;
import cr.ac.una.astroline.util.Respuesta;
import java.util.ArrayList;
import java.util.List;

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

    public String getSucursalId() {
        if (configuracion == null || configuracion.getSucursalId() == null) {
            return "sucursal-1";
        }
        return configuracion.getSucursalId();
    }

    public String getEstacionId() {
        if (configuracion == null) {
            return null;
        }
        return configuracion.getEstacionId();
    }

    public boolean isPreferencial() {
        if (configuracion == null) {
            return false;
        }
        return configuracion.isPreferencial();
    }

    public Respuesta guardarConfiguracion(String sucursalId, String estacionId, List<String> tramiteIds, boolean preferencial) {
        try {
            if (sucursalId == null || sucursalId.isBlank()) {
                return new Respuesta(false, "El id de sucursal no puede estar vacío.", "");
            }

            configuracion = new ConfiguracionLocal(sucursalId, estacionId, tramiteIds, preferencial);
            GsonUtil.guardar(configuracion, ARCHIVO_JSON);

            return new Respuesta(true, "Configuración guardada.", "", "configuracion", configuracion);

        } catch (Exception e) {
            return new Respuesta(false, "No se pudo guardar la configuración.", "ConfiguracionService.guardarConfiguracion > " + e.getMessage());
        }
    }

    public Respuesta resetearConfiguracion() {
        try {
            configuracion = new ConfiguracionLocal();
            GsonUtil.guardar(configuracion, ARCHIVO_JSON);
            return new Respuesta(true, "Configuración restablecida.", "", "configuracion", configuracion);
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo restablecer la configuración.",
                    "ConfiguracionService.resetearConfiguracion > " + e.getMessage());
        }
    }

    private void cargarConfiguracion() {
        configuracion = GsonUtil.leer(ARCHIVO_JSON, ConfiguracionLocal.class);
    }

    public void recargarConfiguracion() {
        configuracion = GsonUtil.leer(ARCHIVO_JSON, ConfiguracionLocal.class);
    }

    public List<String> getTramitesConfigurados() {
        if (configuracion == null || configuracion.getTramiteIds() == null) {
            return new ArrayList<>();
        }

        TramiteService ts = TramiteService.getInstancia();

        List<String> tramites = new ArrayList<>();

        for (String id : configuracion.getTramiteIds()) {
            Tramite t = ts.buscarPorId(id);
            if (t != null) {
                tramites.add(id);
            }
        }
        return tramites;
    }
}
