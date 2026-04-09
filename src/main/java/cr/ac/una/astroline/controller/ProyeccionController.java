package cr.ac.una.astroline.controller;

import cr.ac.una.astroline.model.Ficha;
import cr.ac.una.astroline.util.*;
import cr.ac.una.astroline.service.FichaService;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Controller placeholder del módulo Kiosko.
 * Será implementado por todos.
 *
 * */
public class ProyeccionController extends Controller implements Initializable {

    @FXML
    private BorderPane root;
    
    FichaService fichas = new FichaService();
    
    ArrayList<Ficha> fichasEnColaAtender;
    
    
    @FXML
    private Label lblSucursalInfo;
    @FXML
    private VBox primerFichaEspera;
    @FXML
    private VBox segundaFichaEspera;
    @FXML
    private VBox terceraFichaEspera;
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

    @Override
    public void initialize() {
        setNombreVista("Proyeccion");
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarFichasPorAtender();
        //cargarFichasAtendidas();
    }    
    
    
/*
*       Cargar vistas de las fichas 
*/
    
    private void cargarFichasPorAtender(){
        
        Respuesta resUltimasFichas = fichas.obtenerFichasActivas();


        if(resUltimasFichas == null || !resUltimasFichas.getEstado())return;

        List<Ficha> fichasAMostrar = ordenarFichasPorPrioridad((List<Ficha>)resUltimasFichas.getResultado("lista"));
        int size = fichasAMostrar.size();
        cargarFicha1(fichasAMostrar.get(0));
        cargarFicha2(fichasAMostrar.get(1));
        cargarFicha3(fichasAMostrar.get(2));
        
        
        
    }     
           
 // Metodos a considerar para implementar en Fichas service   
    
    
    
    /**
     *
     * Compara y ordena en base a la prioridad, tomando en cuenta si es preferencial o por codigo de ticket
     * 
     */
    private List<Ficha> ordenarFichasPorPrioridad(List<Ficha> fichasAOrdenar){
            
      if(fichasAOrdenar == null) return null;  
        
      
      Comparator<Ficha> comparador = (ficha1, ficha2) -> {
          
          if(ficha1.isPreferencial() && !ficha2.isPreferencial()) return -1;
          if(!ficha1.isPreferencial() && ficha2.isPreferencial()) return 1;

          if(ficha1.getId().charAt(0) < ficha2.getId().charAt(0)) return -1;
          if(ficha1.getId().charAt(0) > ficha2.getId().charAt(0)) return 1;
          
          if(ficha1.getNumero() < ficha2.getNumero()) return -1;
          if(ficha1.getNumero() > ficha2.getNumero()) return 1;
          
          return 0;
      };
      
              
              
      return fichasAOrdenar.stream().
              filter(f -> f.estaEsperando()).
              sorted(comparador).limit(3).toList();
    }
    
    /**
     *
     * Cargado de las fichas 
     * 
     */
    
    private void cargarFicha1(Ficha ficha){
        
        lblEsperandoCode1.setText(ficha.getCodigo());
                
        if(!ficha.isPreferencial()){
            imgEsperandoPref1.setVisible(false);
            imgEsperandoPref1.setManaged(false);
        }
        
        if(ficha.getCedulaCliente() != null) lblEsperandoCed1.setText(ficha.getCedulaCliente());
        else {
            lblEsperandoCed1.setVisible(false);
            lblEsperandoCed1.setManaged(false);
        }
        
    }
      private void cargarFicha2(Ficha ficha){
        
        lblEsperandoCode2.setText(ficha.getCodigo());
                
        if(!ficha.isPreferencial()){
            imgEsperandoPref2.setVisible(false);
            imgEsperandoPref2.setManaged(false);
        }
        
        if(ficha.getCedulaCliente() != null) lblEsperandoCed2.setText(ficha.getCedulaCliente());
        else {
            lblEsperandoCed2.setVisible(false);
            lblEsperandoCed2.setManaged(false);
        }
        
    }
        private void cargarFicha3(Ficha ficha){
        
        lblEsperandoCode3.setText(ficha.getCodigo());
                
        if(!ficha.isPreferencial()){
            imgEsperandoPref3.setVisible(false);
            imgEsperandoPref3.setManaged(false);
        }
        
        if(ficha.getCedulaCliente() != null) lblEsperandoCed3.setText(ficha.getCedulaCliente());
        else {
            lblEsperandoCed3.setVisible(false);
            lblEsperandoCed3.setManaged(false);
        }
        
    }
    
    
    
}