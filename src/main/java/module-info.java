module cr.ac.una.astroline {
    requires javafx.controls;
    requires javafx.fxml;

    opens cr.ac.una.astroline to javafx.fxml;
    exports cr.ac.una.astroline;
}
