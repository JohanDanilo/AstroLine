package cr.ac.una.astroline;


import cr.ac.una.astroline.util.DataInitializer;
import cr.ac.una.astroline.util.FlowController;
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
    private static String acceso = "Kiosko";

    @Override
    public void start(Stage stage) throws Exception {
        DataInitializer.inicializar();        
        FlowController.getInstance().InitializeFlow(stage, null);
        if ("Funcionario".equals(acceso)) {
            acceso = "Login" + acceso;
        }
        FlowController.getInstance().goMain(acceso);
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            acceso = args[0];
        }
        launch(args);
    }

}