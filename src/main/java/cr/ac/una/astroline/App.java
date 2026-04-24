package cr.ac.una.astroline;

import cr.ac.una.astroline.service.ConfiguracionService;
import cr.ac.una.astroline.util.DataInitializer;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.PathManager;
import cr.ac.una.astroline.util.SessionManager;
import java.nio.file.Files;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;

public class App extends Application {

    private static Scene scene;
    private static String acceso = "";

    @Override
    public void start(Stage stage) throws Exception {
        DataInitializer.inicializar();
        FlowController.getInstance().InitializeFlow(stage, null);

        String modo = (acceso == null || acceso.isBlank()) ? "" : acceso.trim().toLowerCase();
        
        System.out.println("[DEBUG] Config path absoluto: " + 
        PathManager.getGlobalConfigPath().toAbsolutePath());
        System.out.println("[DEBUG] Archivo existe: " + 
        Files.exists(PathManager.getGlobalConfigPath()));
        System.out.println("[DEBUG] sucursalId: " + 
            ConfiguracionService.getInstancia().getSucursalId());

        switch (modo) {
            case "admin" -> {
                SessionManager.getInstancia().setModoAcceso("admin");
                FlowController.getInstance().goViewInWindow("LoginFuncionarioView");
            }
            case "funcionario" -> {
                SessionManager.getInstancia().setModoAcceso("funcionario");
                FlowController.getInstance().goViewInWindow("LoginFuncionarioView");
            }
            case "kiosko" -> {
                SessionManager.getInstancia().setModoAcceso("kiosko");
                // Guard: si no hay sucursal configurada, pedir al usuario que la elija
                if (!ConfiguracionService.getInstancia().estaConfiguradoParaModo("kiosko")) {
                    FlowController.getInstance().goViewInWindow("SeleccionSucursalView");
                } else {
                    FlowController.getInstance().goMain("Kiosko");
                }
            }
            case "proyeccion" -> {
                SessionManager.getInstancia().setModoAcceso("proyeccion");
                // Guard: si no hay sucursal configurada, pedir al usuario que la elija
                if (!ConfiguracionService.getInstancia().estaConfiguradoParaModo("proyeccion")) {
                    FlowController.getInstance().goViewInWindow("SeleccionSucursalView");
                } else {
                    FlowController.getInstance().goMain("Proyeccion");
                }
            }
            default ->
                FlowController.getInstance().goViewInWindow("PrincipalView");
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            acceso = args[0];
        }
        launch(args);
    }
}