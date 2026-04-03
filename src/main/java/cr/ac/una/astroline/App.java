package cr.ac.una.astroline;

import atlantafx.base.theme.PrimerDark;
import cr.ac.una.astroline.util.DataInitializer;
import cr.ac.una.astroline.util.FlowController;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Clase principal de la aplicación AstroLine.
 * Inicializa el tema visual y delega la navegación al FlowController.
 *
 * @author JohanDanilo
 */

public class App extends Application {
    
    private static Scene scene;
    private static String acceso = "LoginFuncionario";

    @Override
    public void start(Stage stage) throws Exception {
        DataInitializer.inicializar();
        
        stage.setTitle("AstroLine");        
        FlowController.getInstance().InitializeFlow(stage, null);
        FlowController.getInstance().goMain(acceso);
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("view/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            acceso = args[0];
        }
        launch(args);
    }

}