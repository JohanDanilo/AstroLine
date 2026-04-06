package cr.ac.una.astroline.util;

import cr.ac.una.astroline.model.ConfiguracionLocal;
import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.model.Estacion;
import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.model.Tramite;
import java.util.ArrayList;
import java.util.List;

/**
 * Se ejecuta una sola vez al arrancar la app desde App.java.
 * Crea los archivos JSON faltantes en data/ con contenido inicial.
 * Si el archivo ya existe, no lo toca.
 *
 * @author JohanDanilo
 */
public class DataInitializer {

    private DataInitializer() {
    }

    public static void inicializar() {
        inicializarEmpresa();
        inicializarTramites();
        inicializarFichas();
        inicializarHistorial();
        inicializarClientes();
        inicializarSucursales();
        inicializarConfiguracion();
    }

    private static void inicializarConfiguracion() {
        if (GsonUtil.existe("configuracion.json")) return;
        ConfiguracionLocal config = new ConfiguracionLocal(); // constructor vacío, todo null
        GsonUtil.guardar(config, "configuracion.json");
        System.out.println("[DataInitializer] configuracion.json creado vacío.");
    }

    private static void inicializarSucursales() {
        if (GsonUtil.existe("sucursales.json")) return;

        Sucursal sucursal = new Sucursal("sucursal-1", "Sucursal Central");
        sucursal.setTextoAviso("Bienvenido a AstroLine. Por favor espere su turno.");

        // Estación inicial que atiende todos los trámites
        Estacion estacion = new Estacion("estacion-1", "Estación 1", "sucursal-1", false, true);
        estacion.agregarTramite("A");
        estacion.agregarTramite("B");
        estacion.agregarTramite("C");
        sucursal.agregarEstacion(estacion);

        List<Sucursal> lista = new ArrayList<>();
        lista.add(sucursal);

        GsonUtil.guardar(lista, "sucursales.json");
        System.out.println("[DataInitializer] sucursales.json creado.");
    }

    private static void inicializarClientes() {
        if (GsonUtil.existe("clientes.json")) return;
        GsonUtil.guardar(new ArrayList<>(), "clientes.json");
        System.out.println("[DataInitializer] clientes.json creado vacío.");
    }
    
    private static void inicializarEmpresa() {
        if (GsonUtil.existe("empresa.json")) return;
        Empresa empresa = new Empresa();
        empresa.setNombre("Astroline Corp");
        empresa.setLogoPath("assets/logo.png");
        empresa.setPinAdmin("1234"); // pin por defecto
        empresa.setTelefono("");
        empresa.setCorreo("");
        empresa.setDireccion("");
        GsonUtil.guardar(empresa, "empresa.json");
        System.out.println("[DataInitializer] empresa.json creado.");
    }

    private static void inicializarTramites() {
        if (GsonUtil.existe("tramites.json")) return;

        List<Tramite> lista = new ArrayList<>();

        // El id ES la letra del prefijo de ficha
        lista.add(new Tramite("A", "Pasaporte", "Trámites relacionados a pasaportes", true));
        lista.add(new Tramite("B", "Licencia", "Trámites relacionados a licencias", true));
        lista.add(new Tramite("C", "Cédula", "Trámites relacionados a cédulas", true));

        GsonUtil.guardar(lista, "tramites.json");

        System.out.println("[DataInitializer] tramites.json creado.");
    }

    private static void inicializarFichas() {
        if (GsonUtil.existe("fichas.json")) return;

        GsonUtil.guardar(new ArrayList<>(), "fichas.json");

        System.out.println("[DataInitializer] fichas.json creado vacío.");
    }

    private static void inicializarHistorial() {
        if (GsonUtil.existe("historial.json")) return;

        GsonUtil.guardar(new ArrayList<>(), "historial.json");

        System.out.println("[DataInitializer] historial.json creado vacío.");
    }
}