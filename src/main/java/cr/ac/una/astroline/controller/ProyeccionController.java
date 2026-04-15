package cr.ac.una.astroline.controller;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
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
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
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
public class ProyeccionController extends Controller implements Initializable {

    @FXML
    private BorderPane root;
    
    FichaService fichas = new FichaService();
    
    List<Ficha> listaFichas;
    
    Ficha fichaLlamada;
    
    List<Label> lblCodesEnEspera;
    List<Label> lblCedsEnEspera;
    List<ImageView> imgsPrefEnEspera;
    
    
    List<Label> lblCodesAtendidas;
    List<Label> lblEstacionesAtendidas;
    List<Label> lblCedsAtendidas;
    List<ImageView> imgsPrefAtendidas;

    private static final ZoneId ZONA_CR = ZoneId.of("America/Costa_Rica");

    @FXML
    private ImageView imgEsperandoPref1;
    @FXML
    private Label lblEsperandoCode1;
    @FXML
    private Label lblEsperandoCode2;
    @FXML
    private Label lblEsperandoCode3;
    @FXML
    private Label lblEsperandoCed1;
    @FXML
    private Label lblEsperandoCed2;
    @FXML
    private Label lblEsperandoCed3;
    @FXML
    private ImageView imgEsperandoPref2;
    @FXML
    private ImageView imgEsperandoPref3;
    @FXML
    private ImageView imgLogo;
    @FXML
    private Label lblAtendiendoCode1;
    @FXML
    private Label lblAtendiendoCed1;
    @FXML
    private Label lblAtendiendoEstacion1;
    @FXML
    private ImageView imgAtendiendoPref1;
    @FXML
    private Label lblAtendiendoCode2;
    @FXML
    private Label lblAtendiendoCed2;
    @FXML
    private Label lblAtendiendoEstacion2;
    @FXML
    private ImageView imgAtendiendoPref2;
    @FXML
    private Label lblAtendiendoCode3;
    @FXML
    private Label lblAtendiendoCed3;
    @FXML
    private Label lblAtendiendoEstacion3;
    @FXML
    private ImageView imgAtendiendoPref3;
    @FXML
    private Label lblFechaYHora;
    @FXML
    private Label lblEmpresa;

    @Override
    public void initialize() {
        setNombreVista("Proyeccion");
        cargarEmpresa();
        cargarHoraYFecha();
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //____________Ordenar Recursos de UI - Fichas en espera
        lblCodesEnEspera = List.of(lblEsperandoCode1, lblEsperandoCode2, lblEsperandoCode3);
        lblCedsEnEspera = List.of(lblEsperandoCed1, lblEsperandoCed2, lblEsperandoCed3);
        imgsPrefEnEspera = List.of(imgEsperandoPref1, imgEsperandoPref2, imgEsperandoPref3); 
        

        //____________Ordenar Recursos de UI - Fichas Atendidas        
        lblCodesAtendidas = List.of(lblAtendiendoCode1, lblAtendiendoCode2, lblAtendiendoCode3);
        lblEstacionesAtendidas = List.of(lblAtendiendoEstacion1, lblAtendiendoEstacion2, lblAtendiendoEstacion3);
        lblCedsAtendidas = List.of(lblAtendiendoCed1, lblAtendiendoCed2, lblAtendiendoCed3);
        imgsPrefAtendidas = List.of(imgAtendiendoPref1, imgAtendiendoPref2, imgAtendiendoPref3);
        
        iniciarActualizacionDeFichas();
        
    }    
    
    //__________________ ACTUALIZACION DE FICHAS CONSTANTE
    private void iniciarActualizacionDeFichas(){
       // IMPLEMENTAR DATANOTIFIER DE FORMA EFICIENTE
        Timeline actualizacion = new Timeline(
                new KeyFrame(Duration.seconds(10), e -> {
                    limpiarUI();
                    cargarFichas();
            })
        );
        
        actualizacion.setCycleCount(Timeline.INDEFINITE);
        actualizacion.play();
    }
    
    //__________________ CARGADO DE FICHAS
    private void cargarFichas(){
        
        Respuesta res = fichas.obtenerFichasActivas();
        
        if( res == null || !res.getEstado()) return;
        
        List<Ficha> listaFichasActivas = (List)res.getResultado("lista");
        
        listaFichas = listaFichasActivas;
        
        limpiarUI();
        
        cargarFichasEnEsperaAUI();
        cargarFichasAtendidasAUI();
    }
    
    
    //__________________ CARGAR FICHAS A LA UI
    
    private void cargarFichasEnEsperaAUI(){
        
        List<Ficha> filtrada = obtenerFichasEnEsperaParaUI();
        
        for (int i = 0; i < filtrada.size(); i++)
            actualizarFichaEnEspera(filtrada.get(i), 
                    lblCodesEnEspera.get(i), 
                    lblCedsEnEspera.get(i), 
                    imgsPrefEnEspera.get(i));
        
        
        
    }     
    
    private void cargarFichasAtendidasAUI(){
        
        List<Ficha> filtrada = obtenerFichasAtendidasParaUI();
     
        for(int i = 0; i < filtrada.size(); i++)
            actualizarFichaAtendida(filtrada.get(i),
                    lblCodesAtendidas.get(i),
                    lblCedsAtendidas.get(i),
                    lblEstacionesAtendidas.get(i), 
                    imgsPrefAtendidas.get(i));
    }
    
        
    // __________________ FILTRAR FICHAS SEGUN EL ESTADO
    private List<Ficha> obtenerFichasEnEsperaParaUI(){
        
        List<Ficha> listaEsperando = new ArrayList();
        
        for (Ficha actual : listaFichas)
            if(actual.estaEsperando())
                listaEsperando.add(actual);
        
        listaEsperando.sort((ficha1, ficha2) -> {
          
          if(ficha1.isPreferencial() && !ficha2.isPreferencial()) return -1;
          if(!ficha1.isPreferencial() && ficha2.isPreferencial()) return 1;

          if(ficha1.getId().charAt(0) < ficha2.getId().charAt(0)) return -1;
          if(ficha1.getId().charAt(0) > ficha2.getId().charAt(0)) return 1;
          
          if(ficha1.getNumero() < ficha2.getNumero()) return -1;
          if(ficha1.getNumero() > ficha2.getNumero()) return 1;
          
          return 0;
      });
              
        List<Ficha> ordenada = new ArrayList<>();
        
        for(int i = 0; i < listaEsperando.size() && i < 3; i++)
            ordenada.add(listaEsperando.get(i));
        
        return ordenada;
    
    }
    
    private List<Ficha> obtenerFichasAtendidasParaUI(){
        
        List<Ficha> listaAtendidas = new ArrayList<>();
        
        for(Ficha actual : listaFichas)
            if(actual.getEstado() == Ficha.Estado.ATENDIDA)
                listaAtendidas.add(actual);
        
        listaAtendidas.sort((ficha1, ficha2) -> {
            
          ZonedDateTime timeFicha1 = ZonedDateTime.parse(ficha1.getFechaHoraLlamado());
          ZonedDateTime timeFicha2 = ZonedDateTime.parse(ficha2.getFechaHoraLlamado());
          
          return timeFicha2.compareTo(timeFicha1);
          
        });
        
        List<Ficha> ordenada = new ArrayList<>();
        
        for (int i = 0; i < listaAtendidas.size() && i < 3; i++)
            ordenada.add(listaAtendidas.get(i));
        
        return ordenada;
    }
    
    
    //__________________ ACTUALIZAR FICHA EN UI SEGUN EL ESTADO
    private void actualizarFichaEnEspera(Ficha ficha, Label lblCode, 
            Label lblCed, ImageView imgPref){
        
        if(ficha == null) return;
        
        lblCode.setText(ficha.getCodigo());
        
        imgPref.setVisible(ficha.isPreferencial());
        imgPref.setManaged(ficha.isPreferencial());
        
        if(ficha.getCedulaCliente() != null){
            lblCed.setVisible(true);
            lblCed.setManaged(true);
            
            lblCed.setText(ficha.getCedulaCliente());
        }
            
    }
    
    private void actualizarFichaAtendida(Ficha ficha, Label lblCode, Label lblCed, 
            Label lblEstacion, ImageView imgPref){
        if(ficha == null) return;
        
        lblCode.setText(ficha.getCodigo());
        lblEstacion.setText(ficha.getEstacionId());
        
        imgPref.setVisible(ficha.isPreferencial());
        imgPref.setManaged(ficha.isPreferencial());
        
        if(ficha.getCedulaCliente() != null){
            lblCed.setVisible(true);
            lblCed.setManaged(true);
            lblCed.setText(ficha.getCedulaCliente());
        }
        
    }
    
    //__________________ LIMPIEZA DE UI
    
    private void limpiarUI(){
        
        for (int i = 0; i < lblCodesAtendidas.size(); i++){
            
            lblCodesAtendidas.get(i).setText(" Codigo ");
            
            lblCedsAtendidas.get(i).setVisible(false);
            lblCedsAtendidas.get(i).setManaged(false);

            imgsPrefAtendidas.get(i).setVisible(false);
            imgsPrefAtendidas.get(i).setManaged(false);
            
        }
        
        for (int i = 0; i < lblCodesEnEspera.size(); i++){
            lblCodesEnEspera.get(i).setText(" Codigo ");
            lblEstacionesAtendidas.get(i).setText(" Estacion ");
            
            lblCedsEnEspera.get(i).setVisible(false);
            lblCedsEnEspera.get(i).setManaged(false);

            imgsPrefEnEspera.get(i).setVisible(false);
            imgsPrefEnEspera.get(i).setManaged(false);
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
    
    private void verficarLlamadoDeFicha() throws Exception{
        
        Ficha actualFichaLlamada = null;
        
        for(Ficha actual : listaFichas){
            if(actual.getEstado() == Ficha.Estado.LLAMADA){
                actualFichaLlamada = actual;
                break;
            }
        }
                
        if(actualFichaLlamada == null) 
            return;
        
        fichaLlamada = actualFichaLlamada;
        
        actualizarFichaEnEspera(fichaLlamada, lblAtendiendoCode1, lblAtendiendoCed1, imgAtendiendoPref1);
        
        PiperTTSService.getInstancia().hablar(fichaLlamada.obtenerMensajeDeLlamda());
        
        //EL ESTADO DE LA FICHA LO CAMBIARA DIRECTAMENTE FUNCIONARIO, ESTA SOLO SE ENCARGAR DE RECIBIR LOS CAMBIOS 
        
    }
    
    
}