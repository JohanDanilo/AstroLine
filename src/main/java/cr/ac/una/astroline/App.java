package cr.ac.una.astroline;

import atlantafx.base.theme.PrimerDark;
import cr.ac.una.astroline.util.DataInitializer;
import cr.ac.una.astroline.util.FlowController;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Clase principal de la aplicación AstroLine.
 * Inicializa el tema visual y delega la navegación al FlowController.
 *
 * @author JohanDanilo
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        
        DataInitializer.inicializar();

        // Cargar el logo como ícono de la ventana
        Image logo = new Image(
            App.class.getResourceAsStream("/cr/ac/una/astroline/resource/logo.png")
        );
        stage.getIcons().add(logo);
        stage.setTitle("AstroLine");

        // Aplicar tema visual de AtlantaFX
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        // Inicializar el FlowController con el stage principal
        FlowController.getInstance().InitializeFlow(stage, null);

        // Navegar a la pantalla principal
        FlowController.getInstance().goMain();
    }

    public static void main(String[] args) {
        launch();
    }
}