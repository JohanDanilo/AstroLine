package cr.ac.una.astroline.util;

import com.google.gson.JsonObject;
import cr.ac.una.astroline.model.ConfiguracionLocal;
import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.model.Estacion;
import cr.ac.una.astroline.model.Funcionario;
import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.model.Tramite;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DataInitializer {

    private DataInitializer() {
    }

    public static void inicializar() {
        inicializarProperties();
        inicializarConfiguracion();
        inicializarEmpresa();
        inicializarTramites();
        inicializarFichas();
        inicializarHistorial();
        inicializarClientes();
        inicializarFuncionarios();
        inicializarSucursales();
    }
    

    private static void inicializarProperties() {
        Path path = PathManager.getPropertiesPath();
        if (Files.exists(path)) {
            return;
        }
        try {
            JsonObject obj = new JsonObject();
            obj.addProperty("rutaDatos",
                    Path.of(System.getProperty("user.home"), "Documents", "AstroLine").toString());
            Files.writeString(path, GsonUtil.getGson().toJson(obj));
            System.out.println("[DataInitializer] properties.json creado en: "
                    + path.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[DataInitializer] No se pudo crear properties.json: "
                    + e.getMessage());
        }
    }

    private static void inicializarConfiguracion() {
        Path path = PathManager.getGlobalConfigPath();
        if (Files.exists(path)) {
            return;
        }
        try {
            ConfiguracionLocal config = new ConfiguracionLocal();
            Files.writeString(path, GsonUtil.getGson().toJson(config));
            System.out.println("[DataInitializer] configuracion.json creado en: "
                    + path.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[DataInitializer] No se pudo crear configuracion.json: "
                    + e.getMessage());
        }
    }

    private static void inicializarSucursales() {
        if (GsonUtil.existe("sucursales.json")) {
            return;
        }

        Sucursal sucursal = new Sucursal("sucursal-1", "Sucursal Central");
        sucursal.setTextoAviso("Bienvenido a AstroLine. Por favor espere su turno.");

        Estacion estacion = new Estacion("E-1-1", "Estación 1", "sucursal-1", false, true);
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
        if (GsonUtil.existe("clientes.json")) {
            return;
        }
        GsonUtil.guardar(new ArrayList<>(), "clientes.json");
        System.out.println("[DataInitializer] clientes.json creado vacío.");
    }

    private static void inicializarFuncionarios() {
        if (GsonUtil.existe("funcionarios.json")) {
            return;
        }

        Funcionario funcionario = new Funcionario();
        funcionario.setNombre("Administrador");
        funcionario.setApellidos("General");
        funcionario.setUsername("admin");
        funcionario.setPassword("1234");
        funcionario.setAdmin(true);
        funcionario.setCedula("000000001");
        funcionario.setLastModified(System.currentTimeMillis());

        List<Funcionario> lista = new ArrayList<>();
        lista.add(funcionario);
        GsonUtil.guardar(lista, "funcionarios.json");

        System.out.println("[DataInitializer] funcionarios.json creado con funcionario predeterminado.");
    }

    private static void inicializarEmpresa() {
        if (GsonUtil.existe("empresa.json")) {
            return;
        }
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
        if (GsonUtil.existe("tramites.json")) {
            return;
        }

        List<Tramite> lista = new ArrayList<>();

        lista.add(new Tramite("A", "Pasaporte", "Trámites relacionados a pasaportes", true));
        lista.add(new Tramite("B", "Licencia", "Trámites relacionados a licencias", true));
        lista.add(new Tramite("C", "Cédula", "Trámites relacionados a cédulas", true));

        GsonUtil.guardar(lista, "tramites.json");

        System.out.println("[DataInitializer] tramites.json creado.");
    }

    private static void inicializarFichas() {
        if (GsonUtil.existe("fichas.json")) {
            return;
        }

        GsonUtil.guardar(new ArrayList<>(), "fichas.json");

        System.out.println("[DataInitializer] fichas.json creado vacío.");
    }

    private static void inicializarHistorial() {
        if (GsonUtil.existe("historial.json")) {
            return;
        }

        GsonUtil.guardar(new ArrayList<>(), "historial.json");

        System.out.println("[DataInitializer] historial.json creado vacío.");
    }

}
