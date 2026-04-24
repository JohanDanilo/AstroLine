package cr.ac.una.astroline.service;

import cr.ac.una.astroline.model.Estacion;
import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.model.Tramite;
import cr.ac.una.astroline.util.GsonUtil;
import cr.ac.una.astroline.util.Respuesta;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SucursalService{

    private static final String ARCHIVO_JSON = "sucursales.json";
    private final ObservableList<Sucursal> listaDeSucursales;
    private static SucursalService instancia;

    private SucursalService() {
        listaDeSucursales = FXCollections.observableArrayList();
        cargarSucursales();
    }

    public static SucursalService getInstancia() {
        if (instancia == null) {
            instancia = new SucursalService();
        }
        return instancia;
    }

    public ObservableList<Sucursal> getListaDeSucursales() {
        return listaDeSucursales;
    }

    public Sucursal buscarSucursal(String sucursalId) {
        if (sucursalId == null) {
            return null;
        }
        for (Sucursal s : listaDeSucursales) {
            if (s.getId().equals(sucursalId)) {
                return s;
            }
        }
        return null;
    }

    public boolean existeSucursal(String sucursalId) {
        return buscarSucursal(sucursalId) != null;
    }

    public Respuesta agregarSucursal(Sucursal sucursal) {
        try {
            if (sucursal == null) {
                return new Respuesta(false, "La sucursal no puede ser nula.", "");
            }
            if (existeSucursal(sucursal.getId())) {
                return new Respuesta(false, "Ya existe una sucursal con ese id.", "");
            }

            sucursal.marcarModificada();
            listaDeSucursales.add(sucursal);
            guardar();
            return new Respuesta(true, "Sucursal agregada.", "", "sucursal", sucursal);
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo agregar la sucursal.",
                    "SucursalService.agregarSucursal > " + e.getMessage());
        }
    }

    public Respuesta actualizarSucursal(Sucursal sucursalActualizada) {
        try {
            if (sucursalActualizada == null) {
                return new Respuesta(false, "La sucursal no puede ser nula.", "");
            }

            for (int i = 0; i < listaDeSucursales.size(); i++) {
                if (listaDeSucursales.get(i).getId().equals(sucursalActualizada.getId())) {
                    sucursalActualizada.marcarModificada();
                    listaDeSucursales.set(i, sucursalActualizada);
                    guardar();
                    return new Respuesta(true, "Sucursal actualizada.",
                            "", "sucursal", sucursalActualizada);
                }
            }
            return new Respuesta(false, "Sucursal no encontrada.", "");
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo actualizar la sucursal.",
                    "SucursalService.actualizarSucursal > " + e.getMessage());
        }
    }

    public Respuesta eliminarSucursal(String sucursalId) {
        try {
            Sucursal encontrada = buscarSucursal(sucursalId);
            if (encontrada == null) {
                return new Respuesta(false, "Sucursal no encontrada.", "");
            }

            listaDeSucursales.remove(encontrada);
            guardar();
            return new Respuesta(true, "Sucursal eliminada.", "");
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo eliminar la sucursal.",
                    "SucursalService.eliminarSucursal > " + e.getMessage());
        }
    }

    public Estacion buscarEstacion(String estacionId) {
        if (estacionId == null) {
            return null;
        }
        for (Sucursal s : listaDeSucursales) {
            Estacion e = s.buscarEstacion(estacionId);
            if (e != null) {
                return e;
            }
        }
        return null;
    }

    public List<Estacion> getEstacionesDeSucursal(String sucursalId) {
        Sucursal s = buscarSucursal(sucursalId);
        return s != null ? s.getEstaciones() : new ArrayList<>();
    }

    public Respuesta agregarEstacion(String sucursalId, Estacion estacion) {
        try {
            Sucursal sucursal = buscarSucursal(sucursalId);
            if (sucursal == null) {
                return new Respuesta(false, "Sucursal no encontrada.", "");
            }
            if (estacion == null) {
                return new Respuesta(false, "La estación no puede ser nula.", "");
            }

            boolean agregada = sucursal.agregarEstacion(estacion);
            if (!agregada) {
                return new Respuesta(false, "Ya existe una estación con ese id.", "");
            }

            sucursal.marcarModificada(); // estacion embebida → toca a la sucursal padre
            guardar();
            return new Respuesta(true, "Estación agregada.", "", "estacion", estacion);
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo agregar la estación.", "SucursalService.agregarEstacion > " + e.getMessage());
        }
    }

    public Respuesta actualizarEstacion(String sucursalId, Estacion estacionActualizada) {
        try {
            Sucursal sucursal = buscarSucursal(sucursalId);
            if (sucursal == null) {
                return new Respuesta(false, "Sucursal no encontrada.", "");
            }
            if (estacionActualizada == null) {
                return new Respuesta(false, "La estación no puede ser nula.", "");
            }

            List<Estacion> estaciones = sucursal.getEstaciones();
            for (int i = 0; i < estaciones.size(); i++) {
                if (estaciones.get(i).getId().equals(estacionActualizada.getId())) {
                    estaciones.set(i, estacionActualizada);
                    sucursal.marcarModificada();
                    guardar();
                    return new Respuesta(true, "Estación actualizada.", "", "estacion", estacionActualizada);
                }
            }
            return new Respuesta(false, "Estación no encontrada en la sucursal.", "");
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo actualizar la estación.",
                    "SucursalService.actualizarEstacion > " + e.getMessage());
        }
    }

    public Respuesta eliminarEstacion(String sucursalId, String estacionId) {
        try {
            Sucursal sucursal = buscarSucursal(sucursalId);
            if (sucursal == null) {
                return new Respuesta(false, "Sucursal no encontrada.", "");
            }

            boolean eliminada = sucursal.eliminarEstacion(estacionId);
            if (!eliminada) {
                return new Respuesta(false, "Estación no encontrada.", "");
            }

            sucursal.marcarModificada();
            guardar();
            return new Respuesta(true, "Estación eliminada.", "");
        } catch (Exception e) {
            return new Respuesta(false, "No se pudo eliminar la estación.",
                    "SucursalService.eliminarEstacion > " + e.getMessage());
        }
    }

    public String generarIdEstacion(String sucursalId) {
        Sucursal sucursal = buscarSucursal(sucursalId);
        if (sucursal == null) {
            return null;
        }

        int numSucursal = extraerConsecutivo(sucursalId, "sucursal-");
        String prefijo = "E-" + numSucursal + "-";

        int maxEstacion = 0;
        for (Estacion e : sucursal.getEstaciones()) {
            maxEstacion = Math.max(maxEstacion, extraerConsecutivo(e.getId(), prefijo));
        }

        String candidato;
        do {
            maxEstacion++;
            candidato = prefijo + maxEstacion;
        } while (buscarEstacion(candidato) != null);

        return candidato;
    }

    public String generarIdSucursal() {
        int consecutivoMaximo = 0;
        for (Sucursal sucursal : listaDeSucursales) {
            consecutivoMaximo = Math.max(
                    consecutivoMaximo,
                    extraerConsecutivo(sucursal.getId(), "sucursal-")
            );
        }

        String candidato;
        do {
            consecutivoMaximo++;
            candidato = "sucursal-" + consecutivoMaximo;
        } while (existeSucursal(candidato));

        return candidato;
    }

    private void cargarSucursales() {
        List<Sucursal> lista = GsonUtil.leerLista(ARCHIVO_JSON, Sucursal.class);
        if (lista == null) {
            lista = new ArrayList<>();
        }
        listaDeSucursales.setAll(lista);
    }

    private void guardar() {
        GsonUtil.guardar(new ArrayList<>(listaDeSucursales), ARCHIVO_JSON);
    }

    private int extraerConsecutivo(String id, String prefijo) {
        if (id == null || !id.startsWith(prefijo)) {
            return 0;
        }
        try {
            return Integer.parseInt(id.substring(prefijo.length()));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public List<Tramite> getTramitesDeEstacion(String estacionId) {
        Estacion estacion = buscarEstacion(estacionId);

        if (estacion == null || estacion.getTramiteIds() == null) {
            return new ArrayList<>();
        }

        List<Tramite> tramites = new ArrayList<>();

        for (String tramiteId : estacion.getTramiteIds()) {
            Tramite tramite = TramiteService.getInstancia().buscarPorId(tramiteId);
            if (tramite != null) {
                tramites.add(tramite);
            }
        }

        return tramites;
    }
}
