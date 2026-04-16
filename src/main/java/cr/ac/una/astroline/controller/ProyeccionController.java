package cr.ac.una.astroline.controller;


import cr.ac.una.astroline.App;
import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.model.Ficha;
import cr.ac.una.astroline.util.*;
import cr.ac.una.astroline.service.FichaService;
import cr.ac.una.astroline.service.PiperTTSService;

import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

/**
 * Controller placeholder del módulo Kiosko.
 * Será implementado por todos.
 *
 * */
public class ProyeccionController extends Controller implements Initializable,DataNotifier.Listener {
    
    private FichaService fichas;
        
    private String fichaLlamadaId;
    
    private List<Label> lblCodesAtendidas;
    private List<Label> lblEstacionesAtendidas;
    private List<ImageView> imgsPrefAtendidas;
    
    private List<String> anuncios;
    int indiceDeAnuncios;

    private static final ZoneId ZONA_CR = ZoneId.of("America/Costa_Rica");
    
    private static final String FICHAS_ARCHIVO_JSON = "fichas.json";
    
    @FXML
    private BorderPane root;
    @FXML
    private ImageView imgLogo;
    @FXML
    private Label lblAtendiendoCode1;
    @FXML
    private Label lblAtendiendoEstacion1;
    @FXML
    private ImageView imgAtendiendoPref1;
    @FXML
    private Label lblAtendiendoCode2;
    @FXML
    private Label lblAtendiendoEstacion2;
    @FXML
    private ImageView imgAtendiendoPref2;
    @FXML
    private Label lblAtendiendoCode3;
    @FXML
    private Label lblAtendiendoEstacion3;
    @FXML
    private ImageView imgAtendiendoPref3;
    @FXML
    private Label lblFechaYHora;
    @FXML
    private Label lblEmpresa;
    @FXML
    private Label lblAnuncios;
    @FXML
    private Label lblAtendiendoCode4;
    @FXML
    private Label lblAtendiendoEstacion4;
    @FXML
    private ImageView imgAtendiendoPref4;

    @Override
    public void initialize() {
        setNombreVista("Proyeccion");
        cargarEmpresa();
        cargarHoraYFecha();
        rotarBarraDeAnuncios();
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        DataNotifier.subscribe(this);
        fichas = FichaService.getInstancia();
        

        //____________Ordenar Recursos de UI - Fichas Atendidas        
        lblCodesAtendidas = List.of(lblAtendiendoCode1, lblAtendiendoCode2, lblAtendiendoCode3, lblAtendiendoCode4);
        lblEstacionesAtendidas = List.of(lblAtendiendoEstacion1, lblAtendiendoEstacion2, lblAtendiendoEstacion3, lblAtendiendoEstacion4);
        imgsPrefAtendidas = List.of(imgAtendiendoPref1, imgAtendiendoPref2, imgAtendiendoPref3, imgAtendiendoPref4);
        
        
        cargarFichas();
        
    }    
    
    @Override
    public void onDataChanged(String fileName){
        
        if(!FICHAS_ARCHIVO_JSON.equals(fileName)) 
            return;
        
        System.out.println("[ProyeccionController] Detectando Cambio externo, sincronizando . . .");

        Platform.runLater(() -> {
            cargarFichas();
        });
        
    }
    
    //__________________ CARGADO DE FICHAS
    private void cargarFichas(){
        limpiarUI();
        verficarLlamadoDeFicha();
        cargarFichasAtendidasAUI();
    }
    
    
    //__________________ CARGAR FICHAS A LA UI    
    
    private void cargarFichasAtendidasAUI(){
        int nFichasAMostrar = 4;
        List<Ficha> filtrada = fichas.obtenerFichasAtendidasParaUI(nFichasAMostrar);
     
        for(int i = 0; i < filtrada.size(); i++)
            actualizarFicha(filtrada.get(i),
                    lblCodesAtendidas.get(i),
                    lblEstacionesAtendidas.get(i), 
                    imgsPrefAtendidas.get(i));
    }
    
    
    //__________________ ACTUALIZAR FICHA EN UI SEGUN EL ESTADO
    
    private void actualizarFicha(Ficha ficha, Label lblCode, Label lblEstacion, ImageView imgPref){
        if(ficha == null) return;
        
        lblCode.setText(ficha.getCodigo());
        lblEstacion.setText(ficha.getEstacionId());
        
        imgPref.setVisible(ficha.isPreferencial());
        imgPref.setManaged(ficha.isPreferencial());
       
    }
    
    //__________________ LIMPIEZA DE UI
    
    private void limpiarUI(){
        
        for (int i = 0; i < lblCodesAtendidas.size(); i++){
            
            lblCodesAtendidas.get(i).setText(" Codigo ");

            imgsPrefAtendidas.get(i).setVisible(false);
            imgsPrefAtendidas.get(i).setManaged(false);
            
        }
    }
    
    //__________________ CARGAR DATOS DEL TOP
    
    private void cargarEmpresa(){
        
        Empresa empresa = GsonUtil.leer("empresa.json", Empresa.class);
          if (empresa == null) return;

        lblEmpresa.setText(empresa.getNombre());

        if (empresa.getLogoPath() != null && !empresa.getLogoPath().isBlank()) {
            try {
                var stream = App.class.getResourceAsStream(
                        "/cr/ac/una/astroline/resource/"
                        + empresa.getLogoPath().replace("assets/", ""));
                if (stream != null) {
                    imgLogo.setImage(new Image(stream));
                }
            } catch (Exception e) {
                System.err.println("[KioskoController] Logo no encontrado: " + e.getMessage());
            }
        }
        
    }
    
    private void cargarHoraYFecha(){
        
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        
        Timeline tiempo = new Timeline (
                new KeyFrame(Duration.seconds(1), e-> {
                  ZonedDateTime horaActual = ZonedDateTime.now(ZONA_CR);
                  lblFechaYHora.setText(horaActual.format(formatoFecha));
                })
        );
        tiempo.setCycleCount(Timeline.INDEFINITE);
        tiempo.play();
    }
    
    //________________ ANUNCIO DEL LLAMADO DE UNA FICHA -- PENDIENTE
    
    private void verficarLlamadoDeFicha(){
        Ficha llamada = FichaService.getInstancia().obtenerFichaLlamada();
        if(llamada == null) return;
        if(llamada.getId().equals(fichaLlamadaId))
            return;
                
        fichaLlamadaId = llamada.getId();
                
        anunciarLlamadoDeFicha(llamada);
                 
    }
    
    private void anunciarLlamadoDeFicha(Ficha ficha){
        try{
            actualizarFicha(ficha, lblAtendiendoCode1,lblAtendiendoEstacion1, imgAtendiendoPref1);
            PiperTTSService.getInstancia().hablar(ficha.obtenerMensajeDeLlamada());
        }
        catch(Exception e){}
    }
    
    
    //________________ Barra de anuncios
    
    private void rotarBarraDeAnuncios(){
        anuncios = List.of("Texto 1",
                 "Texto 2",
                 "Texto 3",
                 "Texto 4",
                 "Texto 5"
                );
        Timeline rotacion = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
                animarBarraDeAnuncios(anuncios.get(indiceDeAnuncios));
                indiceDeAnuncios++;
                
                if(indiceDeAnuncios >= anuncios.size())
                    indiceDeAnuncios = 0;
                }
            )
        );

        rotacion.setCycleCount(Timeline.INDEFINITE);
        rotacion.play();
    }
    
    private void animarBarraDeAnuncios(String texto){
        
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), lblAnuncios);
        fadeOut.setToValue(0);
    
        fadeOut.setOnFinished(a ->{
            lblAnuncios.setText(texto);
            
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), lblAnuncios);
            fadeIn.setToValue(1);
            fadeIn.play();
        });
        
        fadeOut.play();
    }
}