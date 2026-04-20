package cr.ac.una.astroline.controller;


import cr.ac.una.astroline.App;
import cr.ac.una.astroline.model.Empresa;
import cr.ac.una.astroline.model.Ficha;
import cr.ac.una.astroline.service.ConfiguracionService;
import cr.ac.una.astroline.util.*;
import cr.ac.una.astroline.service.FichaService;
import cr.ac.una.astroline.service.SucursalService;
import cr.ac.una.astroline.util.AudioManager;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.TranslateTransition;
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
    
    private String ultimaFichaLlamadaFecha;
    private String ultimaFichaLlamadaId;
    
    private List<Label> lblCodesAtendidas;
    private List<Label> lblEstacionesAtendidas;
    private List<ImageView> imgsPrefAtendidas;
    
    private String textoAviso;

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
        limpiarUI();
        cargarFichasAUI();
        verificarFichaLlamada();     
    }
    
    
    //__________________ CARGAR FICHAS A LA UI    
    
    private void cargarFichasAUI(){
        List<Ficha> filtrada = fichas.obtenerFichasParaProyeccion(sucursalId, false, 4);
     
        if(filtrada == null || filtrada.isEmpty()) return;
        
        for(int i =0 ;i < filtrada.size(); i++)
            actualizarFicha(filtrada.get(i), i);
    }
    
    
    //__________________ ACTUALIZAR FICHA EN UI SEGUN EL ESTADO
    
    private void actualizarFicha(Ficha ficha, int indice){
        if(ficha == null) return;
        
        lblCodesAtendidas.get(indice).setText("Ficha :  " + ficha.getCodigo());
        lblEstacionesAtendidas.get(indice).setText("Estacion :  " + ficha.getTramiteId());
             
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
    
private void verificarFichaLlamada() {
    List<Ficha> recientes = fichas.obtenerFichasParaProyeccion(sucursalId, false, 1);
    
    System.out.println("[DEBUG] recientes: " + (recientes == null ? "null" : recientes.size()));
    
    if (recientes == null || recientes.isEmpty()) return;

    Ficha masReciente = recientes.get(0);

    if (masReciente.getEstacionId() == null) {
        return;
    }

    if (Objects.equals(masReciente.getId(), ultimaFichaLlamadaId)
            && Objects.equals(masReciente.getFechaHoraLlamado(), ultimaFichaLlamadaFecha)) {
        return;
    }

    ultimaFichaLlamadaFecha = masReciente.getFechaHoraLlamado();
    ultimaFichaLlamadaId = masReciente.getId();

    AudioManager.getINSTANCIA().llamarFicha(masReciente);
}    //________________ Barra de anuncios
    
    private void rotarBarraDeAvisos(){
        
        textoAviso = SucursalService.getInstancia().buscarSucursal(sucursalId).getTextoAviso();
        
        if (textoAviso == null || textoAviso.isBlank()) return;
        
        Timeline rotacion = new Timeline(new KeyFrame(Duration.seconds(20), e -> {
                animarTextoDezplazado();   
          }
        ));

        rotacion.setCycleCount(Timeline.INDEFINITE);
        rotacion.play();
    }
    
    private void animarTextoDezplazado(){
        lblAnuncios.setText(textoAviso);
        
        lblAnuncios.setTranslateX(800);
        
        TranslateTransition mov = new TranslateTransition(Duration.seconds(10), lblAnuncios);
        
        mov.setFromX(800);
        mov.setToX(-800);
        
        mov.setCycleCount(TranslateTransition.INDEFINITE);
        mov.play();
    }

}