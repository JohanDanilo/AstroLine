package cr.ac.una.astroline.controller;


import cr.ac.una.astroline.App;
import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.model.Ficha;
import cr.ac.una.astroline.model.Sucursal;
import cr.ac.una.astroline.service.ConfiguracionService;
import cr.ac.una.astroline.util.*;
import cr.ac.una.astroline.service.FichaService;
import cr.ac.una.astroline.service.PiperTTSService;
import cr.ac.una.astroline.service.SucursalService;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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
import javafx.util.Duration;

/**
 * Controller placeholder del módulo Kiosko.
 * Será implementado por todos.
 *
 * */
public class ProyeccionController extends Controller implements Initializable, DataNotifier.Listener{
    
    private FichaService fichas = FichaService.getInstancia();
    private String sucursalId = ConfiguracionService.getInstancia().getSucursalId();
    
    private ZonedDateTime ultimaFechaLlamada;
    private String ultimaFichaLlamadaId;
    
    private List<Label> lblCodesAtendidas;
    private List<Label> lblEstacionesAtendidas;
    private List<ImageView> imgsPrefAtendidas;
    
    private List<String> avisos;
    private int indiceDeAvisos;

    private static final ZoneId ZONA_CR = ZoneId.of("America/Costa_Rica");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter
                            .ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final String ARCHIVO_FICHAS = "fichas.json";
   
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
        rotarBarraDeAvisos();
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        DataNotifier.subscribe(this);
        cargadoInicial();
    }    
    
    // ACCION EN CASO DE CAMBIOS EN FICHAS SERVICE 
    @Override
    public void onDataChanged(String fileName) {
        if(!fileName.equals(ARCHIVO_FICHAS)) return;
        
        System.out.println("[ProyeccionController] Detectado cambio externo, sincronizando...");
        
        Platform.runLater(() -> { actualizarVista(); });
    }
    
    
    //__________________ CARGADO DE FICHAS
    private void cargadoInicial(){
        
        lblCodesAtendidas = List.of(lblAtendiendoCode1, lblAtendiendoCode2, lblAtendiendoCode3, lblAtendiendoCode4);
        lblEstacionesAtendidas = List.of(lblAtendiendoEstacion1, lblAtendiendoEstacion2, lblAtendiendoEstacion3, lblAtendiendoEstacion4);
        imgsPrefAtendidas = List.of(imgAtendiendoPref1, imgAtendiendoPref2, imgAtendiendoPref3, imgAtendiendoPref4);
        actualizarVista();
        
    }
    
    private void actualizarVista(){
        verificarFichaLlamada();
        limpiarUI();
        cargarFichasAUI();
    }
    
    
    //__________________ CARGAR FICHAS A LA UI    
    
    private void cargarFichasAUI(){
        List<Ficha> filtrada = fichas.obtenerFichasParaProyeccion(sucursalId, true, 4);
     
        if(filtrada == null || filtrada.isEmpty()) return;
        
        for(int i = 0; i < filtrada.size(); i++)
            actualizarFicha(filtrada.get(i), i);
    }
    
    
    //__________________ ACTUALIZAR FICHA EN UI SEGUN EL ESTADO
    
    private void actualizarFicha(Ficha ficha, int indice){
        if(ficha == null) return;
        
        lblCodesAtendidas.get(indice).setText(ficha.getCodigo());
        lblEstacionesAtendidas.get(indice).setText(ficha.getEstacionId());
        
        imgsPrefAtendidas.get(indice).setVisible(ficha.isPreferencial());
        imgsPrefAtendidas.get(indice).setManaged(ficha.isPreferencial());
       
    }
    
    
    //__________________ LIMPIEZA DE UI
    
    private void limpiarUI(){
        
        for (int i = 0; i < lblCodesAtendidas.size(); i++){
            
            lblCodesAtendidas.get(i).setText(" Ficha : ");
            lblEstacionesAtendidas.get(i).setText("Estacion : ");
            
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
                System.err.println("[ProyeccionCrontroller] Logo no encontrado: " + e.getMessage());
            }
        }
        
    }
    
    private void cargarHoraYFecha(){
                
        Timeline tiempo = new Timeline (
                new KeyFrame(Duration.seconds(1), e-> {
                  ZonedDateTime horaActual = ZonedDateTime.now(ZONA_CR);
                  lblFechaYHora.setText(horaActual.format(FORMATO_FECHA));
                })
        );
        tiempo.setCycleCount(Timeline.INDEFINITE);
        tiempo.play();
    }
    
    //__________________ VERIFICACION Y CONFRIMACION DE LLAMADO DE FICHAS 
    
    private void verificarFichaLlamada(){
        
        List<Ficha> lista = fichas.obtenerFichasParaProyeccion(sucursalId, true ,4);
        
        for(int i = 0; i < lista.size() && 
                lista.get(i).getEstado() != Ficha.Estado.LLAMADA; i++) 
            anunciarLlamadoDeFicha(lista.get(i));
            
    }
    
    private void anunciarLlamadoDeFicha(Ficha ficha){
        
        if(!puedoVolverALlamar(ficha)) return;
        
        ultimaFechaLlamada = LocalDateTime.parse(ficha.getFechaHoraLlamado(),
                            FORMATO_FECHA).atZone(ZONA_CR);
        ultimaFichaLlamadaId = ficha.getId();
               
        System.out.println("Se ha llamado a la ficha " + ficha.getCodigo());
        PiperTTSService.getInstancia().hablar(ficha.obtenerMensajeDeLlamada());

    }
    
    private boolean puedoVolverALlamar(Ficha aVerificar){
        
        if(aVerificar == null || aVerificar.getFechaHoraLlamado() == null) return false;
       
        
        ZonedDateTime fechaActual =
            LocalDateTime.parse(aVerificar.getFechaHoraLlamado(), 
                    FORMATO_FECHA).atZone(ZONA_CR);

        if(aVerificar.getId().equals(ultimaFichaLlamadaId))
             return ultimaFechaLlamada != null && 
                     fechaActual.isAfter(ultimaFechaLlamada);
        
        return true;
    }
    
    //________________ Barra de anuncios
    
    private void rotarBarraDeAvisos(){
        avisos = List.of("Texto 1",
                 "Texto 2",
                 "Texto 3",
                 "Texto 4",
                 "Texto 5"
                );
        Timeline rotacion = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
                animarBarraDeAvisos(avisos.get(indiceDeAvisos));
                indiceDeAvisos++;
                
                if(indiceDeAvisos >= avisos.size())
                    indiceDeAvisos = 0;
                }
            )
        );

        rotacion.setCycleCount(Timeline.INDEFINITE);
        rotacion.play();
    }
    
    private void animarBarraDeAvisos(String texto){
        
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

    private void cargarAvisos(){
        // Ajustar la rotacino de avisos en base a un solo aviso largoan
        Sucursal sucursal = SucursalService.getInstancia().buscarSucursal(sucursalId);
        
        if(sucursal == null) return;
        
        sucursal.getTextoAviso();
    }
}