module cr.ac.una.astroline {

    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.logging;

    // Librerías UI
    requires atlantafx.base;
    requires MaterialFX;

    // Gson
    requires com.google.gson;
    
    // Pdf
    requires org.apache.pdfbox;   // PDDocument, PDPage, PDType1Font, etc.
    requires java.desktop;         // java.awt.Desktop + java.awt.Color

    // Abre el paquete principal a JavaFX para que pueda leer los FXML
    opens cr.ac.una.astroline to javafx.fxml;

    // Abre los subpaquetes a JavaFX y Gson según necesidad
    opens cr.ac.una.astroline.controller to javafx.fxml;
    opens cr.ac.una.astroline.model to com.google.gson;

    // Exporta los paquetes para que los demás módulos los vean
    exports cr.ac.una.astroline;
    exports cr.ac.una.astroline.controller;
    exports cr.ac.una.astroline.model;
    //exports cr.ac.una.astroline.service;
    exports cr.ac.una.astroline.util;
    requires javafx.graphicsEmpty;
    requires java.base;
}