package cr.ac.una.astroline.util;

import cr.ac.una.astroline.controller.DialogoConfirmacionController;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class Mensaje {


    public void show(AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.show();
    }

    public void showModal(AlertType tipo, String titulo, Window padre, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.initOwner(padre);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public boolean showConfirmation(String titulo, Window padre, String mensaje) {
        return showConfirmation(titulo, padre, mensaje, "Aceptar", "Cancelar");
    }

    public boolean showConfirmation(String titulo, Window padre, String mensaje,
                                    String textoAceptar, String textoCancelar) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/cr/ac/una/astroline/view/DialogoConfirmacionView.fxml")
            );
            Parent root = loader.load();

            DialogoConfirmacionController controller = loader.getController();
            controller.initData(titulo, mensaje, textoAceptar, textoCancelar);

            Stage dialog = new Stage();
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(padre);
            dialog.initStyle(StageStyle.UNDECORATED); // sin barra de título nativa
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            return controller.getResultado();

        } catch (IOException ex) {
            System.err.println("[Mensaje] No se pudo cargar vista Dialogo: " + ex.getMessage());
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.initOwner(padre);
            alert.setContentText(mensaje);
            return alert.showAndWait()
                        .map(b -> b == javafx.scene.control.ButtonType.OK)
                        .orElse(false);
        }
    }
}