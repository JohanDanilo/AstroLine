package cr.ac.una.astroline.service;

import cr.ac.una.astroline.model.ConfiguracionLocal;
import cr.ac.una.astroline.model.Tramite;
import cr.ac.una.astroline.util.GsonUtil;
import cr.ac.una.astroline.util.PathManager;
import cr.ac.una.astroline.util.Respuesta;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ConfiguracionService {

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
            return null;
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
    
    private void cargarConfiguracion() {
    try {
        String json = Files.readString(PathManager.getGlobalConfigPath());
        ConfiguracionLocal leido = GsonUtil.getGson().fromJson(json, ConfiguracionLocal.class);
        configuracion = leido != null ? leido : new ConfiguracionLocal();
    } catch (Exception e) {
        System.err.println("[ConfiguracionService] Error cargando config: " + e.getMessage());
        configuracion = new ConfiguracionLocal();
    }
}

    private void persistirConfiguracion() throws IOException {
        Files.writeString(
                PathManager.getGlobalConfigPath(),
                GsonUtil.getGson().toJson(configuracion)
        );
    }

    public Respuesta guardarConfiguracion(String sucursalId, String estacionId,
                                           List<String> tramiteIds, boolean preferencial) {
        try {
            if (sucursalId == null || sucursalId.isBlank()) {
                return new Respuesta(false, "El id de sucursal no puede estar vacío.", "");
            }
            configuracion = new ConfiguracionLocal(sucursalId, estacionId, tramiteIds, preferencial);
            persistirConfiguracion();
            return new Respuesta(true, "Configuración guardada.", "", "configuracion", configuracion);
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo guardar la configuración.",
                    "ConfiguracionService.guardarConfiguracion > " + e.getMessage());
        }
    }
    
    public Respuesta guardarConfiguracionParaOtrosModos(String sucursalId) {
        try {
            if (sucursalId == null || sucursalId.isBlank()) {
                return new Respuesta(false, "El id de sucursal no puede estar vacío.", "");
            }
            configuracion = new ConfiguracionLocal(sucursalId);
            persistirConfiguracion();
            return new Respuesta(true, "Configuración guardada.", "", "configuracion", configuracion);
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo guardar la configuración.",
                    "ConfiguracionService.guardarConfiguracion > " + e.getMessage());
        }
    }

    public Respuesta resetearConfiguracion() {
        try {
            configuracion = new ConfiguracionLocal();
            persistirConfiguracion();
            return new Respuesta(true, "Configuración restablecida.", "", "configuracion", configuracion);
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo restablecer la configuración.",
                    "ConfiguracionService.resetearConfiguracion > " + e.getMessage());
        }
    }

    public void recargarConfiguracion() {
        cargarConfiguracion();
    }
    
    public boolean estaConfiguradoParaModo(String modo) {
        String sucursalId = configuracion != null ? configuracion.getSucursalId() : null;
        String estacionId = configuracion != null ? configuracion.getEstacionId() : null;

        return switch (modo.toUpperCase()) {
            case "KIOSKO", "PROYECCION" ->
                sucursalId != null && !sucursalId.isBlank();
            case "FUNCIONARIO" ->
                sucursalId != null && !sucursalId.isBlank() &&
                estacionId != null && !estacionId.isBlank();
            default -> true;
        };
    }

    public void setSucursalId(String sucursalId) {
        getConfiguracion().setSucursalId(sucursalId);
    }
    
}
