module cr.ac.una.astroline {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.logging;

    requires javafx.swing;
    requires webcam.capture;

    requires atlantafx.base;
    requires MaterialFX;

    requires com.google.gson;
    
    requires org.apache.pdfbox;
    requires java.desktop;
    
    requires jdk.httpserver;

    opens cr.ac.una.astroline to javafx.fxml;

    opens cr.ac.una.astroline.controller to javafx.fxml;
    opens cr.ac.una.astroline.model to com.google.gson;

    exports cr.ac.una.astroline;
    exports cr.ac.una.astroline.controller;
    exports cr.ac.una.astroline.model;
    exports cr.ac.una.astroline.util;
    requires java.base;
}
