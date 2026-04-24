package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.service.ConfiguracionService;
import cr.ac.una.astroline.service.SucursalService;
import cr.ac.una.astroline.util.FlowController;
import cr.ac.una.astroline.util.SessionManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.util.StringConverter;

public class SeleccionSucursalController extends Controller implements Initializable {

    @FXML
    private MFXComboBox<Sucursal> cmbSeleccionSucursal;
    @FXML
    private MFXButton btnConfirmar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @Override
    public void initialize() {
        setNombreVista("Selección de Sucursal");
        cargarSucursales();
    }

    private void cargarSucursales() {
        cmbSeleccionSucursal.getItems().setAll(SucursalService.getInstancia().getListaDeSucursales());

        cmbSeleccionSucursal.setConverter(new StringConverter<Sucursal>() {
            @Override
            public String toString(Sucursal sucursal) {
                return sucursal != null ? sucursal.getNombre() : "";
            }

            @Override
            public Sucursal fromString(String string) {
                return null;
            }
        });

        // Botón deshabilitado hasta que haya una selección
        btnConfirmar.setDisable(true);
        cmbSeleccionSucursal.valueProperty().addListener((obs, oldVal, newVal) -> {
            btnConfirmar.setDisable(newVal == null);
        });
    }

    @FXML
    private void onConfirmar(ActionEvent event) {
        Sucursal seleccionada = cmbSeleccionSucursal.getValue();
        String seleccionadaId = seleccionada.getId();
        ConfiguracionService cfg = ConfiguracionService.getInstancia();
        cfg.setSucursalId(seleccionada.getId());
        cfg.guardarConfiguracionParaOtrosModos(seleccionadaId);

        navegarAVistaCorrespondiente();
    }

    private void navegarAVistaCorrespondiente() {
        String modo = SessionManager.getInstancia().getModoAcceso();

        // Primero abrir la vista destino
        switch (modo.toUpperCase()) {
            case "KIOSKO" ->
                FlowController.getInstance().goMain("Kiosko");
            case "PROYECCION" ->
                FlowController.getInstance().goMain("Proyeccion");
            default ->
                FlowController.getInstance().goViewInWindow("LoginFuncionarioView");
        }

        // Luego cerrar esta ventana — ya hay algo en pantalla
        if (getStage() != null) getStage().close();
    }
}