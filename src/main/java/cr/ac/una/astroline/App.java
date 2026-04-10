package cr.ac.una.astroline;


import cr.ac.una.astroline.util.DataInitializer;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.SyncManager;
import javafx.scene.Scene;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javafx.application.Application;

/**
 * Clase principal de la aplicación AstroLine.
 * Inicializa el tema visual y delega la navegación al FlowController.
 *
 * @author JohanDanilo
 */

public class App extends Application {
    
    private static Scene scene;
    private static String acceso = "";
    
    
    @Override
    public void start(Stage stage) throws Exception {
        SyncManager.getInstancia().iniciar(); 
        
        DataInitializer.inicializar();    

        FlowController.getInstance().InitializeFlow(stage, null);

        String modo = (acceso == null || acceso.isBlank()) ? "" : acceso.trim().toLowerCase();

        switch (modo) {
            case "admin":
                FlowController.getInstance().goMain("Admin");
                break;

            case "funcionario":
                FlowController.getInstance().goViewInWindow("LoginFuncionarioView");
                break;

            case "kiosko":
                FlowController.getInstance().goMain("Kiosko");
                break;

            case "proyeccion":
                FlowController.getInstance().goMain("Proyeccion");
                break;

            default:
                FlowController.getInstance().goViewInWindow("PrincipalView");
                break;
        }
    }
    
    @Override
    public void stop() throws Exception {
        SyncManager.getInstancia().detener();
        super.stop();
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            acceso = args[0];
        }
        launch(args);
    }

}