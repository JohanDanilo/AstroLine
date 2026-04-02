package cr.ac.una.astroline.util;

import cr.ac.una.astroline.model.Empresa;
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
    }

    private static void inicializarEmpresa() {
        if (GsonUtil.existe("empresa.json")) return;
        Empresa empresa = new Empresa();
        empresa.setNombre("AstroLine Corp");
        empresa.setLogoPath("assets/logo.png");
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