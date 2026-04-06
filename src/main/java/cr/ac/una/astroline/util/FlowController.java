package cr.ac.una.astroline.util;

import cr.ac.una.astroline.App;
import java.io.IOException;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import cr.ac.una.astroline.controller.Controller;
import cr.ac.una.astroline.controller.RegistroClienteController;
import io.github.palexdev.materialfx.css.themes.MFXThemeManager;
import io.github.palexdev.materialfx.css.themes.Themes;
import javafx.scene.layout.HBox;

public class FlowController {

    private static FlowController INSTANCE = null;
    private static Stage mainStage;
    private static ResourceBundle idioma;
    private static HashMap<String, FXMLLoader> loaders = new HashMap<>();

    private FlowController() {
    }

    private static void createInstance() {
        if (INSTANCE == null) {
            synchronized (FlowController.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FlowController();
                }
            }
        }
    }

    public static FlowController getInstance() {
        if (INSTANCE == null) {
            createInstance();
        }
        return INSTANCE;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public void InitializeFlow(Stage stage, ResourceBundle idioma) {
        getInstance();
        this.mainStage = stage;
        this.idioma = idioma;
    }

    private FXMLLoader getLoader(String name) {
        FXMLLoader loader = loaders.get(name);
        if (loader == null) {
            synchronized (FlowController.class) {
                if (loader == null) {
                    try {
                        // CORRECCIÓN: Ruta absoluta desde la raíz de resources
                        loader = new FXMLLoader(App.class.getResource("/cr/ac/una/astroline/view/" + name + ".fxml"), this.idioma);
                        loader.load();
                        loaders.put(name, loader);
                    } catch (Exception ex) {
                        loader = null;
                        java.util.logging.Logger.getLogger(FlowController.class.getName()).log(Level.SEVERE, "Creando loader [" + name + "].", ex);
                    }
                }
            }
        }
        return loader;
    }

    public void goMain() {
        try {
            FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/cr/ac/una/astroline/view/PrincipalView.fxml"), 
                this.idioma
            );
            Parent root = loader.load();

            Controller controller = loader.getController();
            controller.setStage(this.mainStage); // esta es la línea clave
            controller.initialize();

            this.mainStage.setScene(new Scene(root));
            applyIcon(this.mainStage);
            this.mainStage.centerOnScreen();
            this.mainStage.show();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(FlowController.class.getName())
                .log(Level.SEVERE, "Error inicializando la vista base.", ex);
        }
    }
    
    public void goMain(String acceso) {
        try {
            if (acceso == null || acceso.isBlank()) {
                acceso = "Principal"; // fallback seguro
            }

            FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/cr/ac/una/astroline/view/" + acceso + "View.fxml"), 
                this.idioma
            );

            Parent root = loader.load();

            Controller controller = loader.getController();
            controller.setStage(this.mainStage);
            controller.initialize();
            this.mainStage.setTitle(acceso);
            this.mainStage.setScene(new Scene(root));
            applyIcon(this.mainStage);
            this.mainStage.centerOnScreen();
            this.mainStage.show();

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(FlowController.class.getName())
                .log(Level.SEVERE, "Error inicializando la vista base.", ex);
        }
    }

    public void goView(String viewName) {
        goView(viewName, "Center", null);

    }

    public void goView(String viewName, String accion) {
        goView(viewName, "Center", accion);
    }

    public void goView(String viewName, String location, String accion) {
        FXMLLoader loader = getLoader(viewName);
        Controller controller = loader.getController();
        controller.initialize();
        Stage stage = controller.getStage();
        if (stage == null) {
            stage = this.mainStage;
            controller.setStage(stage);
        }
        switch (location) {
            case "Center":
                BorderPane borderPane = (BorderPane) stage.getScene().getRoot();
                VBox vBox = (VBox)borderPane.getCenter();
                vBox.getChildren().clear();
                vBox.getChildren().add(loader.getRoot());
                break;
            case "Top":
                BorderPane borderPane2 = (BorderPane) stage.getScene().getRoot();
                HBox hBox = (HBox)borderPane2.getTop();
                hBox.getChildren().clear();
                hBox.getChildren().add(loader.getRoot());
                break;
            default:
                break;
        }
        
        stage.show();
    }

    public void goViewInStage(String viewName, Stage stage) {
        FXMLLoader loader = getLoader(viewName);
        Controller controller = loader.getController();
        controller.setStage(stage);
        stage.getScene().setRoot(loader.getRoot());
    }

    public void goViewInWindow(String viewName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/cr/ac/una/astroline/view/" + viewName + ".fxml"),
                this.idioma
            );
            Parent root = loader.load();
            Controller controller = loader.getController();
            controller.initialize();

            Stage stage = new Stage();
            applyIcon(stage);
            stage.setTitle(controller.getNombreVista());
            stage.setOnHidden((WindowEvent event) -> {
                controller.setStage(null);
            });
            controller.setStage(stage);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(FlowController.class.getName())
                .log(Level.SEVERE, "Error abriendo ventana [" + viewName + "].", ex);
        }
    }

    public void goViewInWindowModal(String viewName, Stage parentStage, Boolean resizable) {
        FXMLLoader loader = getLoader(viewName);
        Controller controller = loader.getController();
        controller.initialize();
        Stage stage = new Stage();
        stage.getIcons().add(new Image(App.class.getResourceAsStream("/cr/ac/una/astroline/resource/logo.png")));
        stage.setTitle(controller.getNombreVista());
        stage.setResizable(resizable);
        stage.setOnHidden((WindowEvent event) -> {
            controller.getStage().getScene().setRoot(new Pane());
            controller.setStage(null);
        });
        controller.setStage(stage);
        Parent root = loader.getRoot();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parentStage);
        stage.centerOnScreen();
        stage.showAndWait();
    }

    public void goViewInDividePane(String viewNameToShow, Pane pane, boolean isActiveDividePane){
	
        if(isActiveDividePane){
            FXMLLoader loader = getLoader(viewNameToShow);
            Controller controller = loader.getController();	
            controller.initialize();
            
            // guarda la referencia del paneprincipal en el registro para poder cerrar la vista desde el registro 
            // Diseño fragil y rigido
            
            if (controller instanceof RegistroClienteController registroController)
                registroController.setPanePadre(pane);
            
            Parent root = loader.getRoot();
            pane.getChildren().clear();
            pane.getChildren().add(root);
        }
        else pane.getChildren().clear();
        
    }
    public Controller getController(String viewName) {
        return getLoader(viewName).getController();
    }

    // FlowController.java - en el método que configura cada Stage
    private void applyIcon(Stage stage) {
        stage.getIcons().clear();
        stage.getIcons().add(new Image(
                App.class.getResourceAsStream("/cr/ac/una/astroline/resource/logo.png")
        ));
    }
    
    public void limpiarLoader(String view){
        this.loaders.remove(view);
    }

    public static void setIdioma(ResourceBundle idioma) {
        FlowController.idioma = idioma;
    }
    
    public void initialize() {
        this.loaders.clear();
    }

    public void salir() {
        this.mainStage.close();
    }
    
      public Stage getMainStage(){
        return mainStage;
    }

}