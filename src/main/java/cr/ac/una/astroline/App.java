package cr.ac.una.astroline;

import cr.ac.una.astroline.util.DataInitializer;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.SessionManager;
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

        switch (modo) {
            case "admin":
                SessionManager.getInstancia().setModoAcceso("admin");
                FlowController.getInstance().goViewInWindow("LoginFuncionarioView");
                break;

            case "funcionario":
                SessionManager.getInstancia().setModoAcceso("funcionario");
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

    public static void main(String[] args) {
        if (args.length > 0) {
            acceso = args[0];
        }
        launch(args);
    }

}
