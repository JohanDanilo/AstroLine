
package cr.ac.una.astroline.model;
import cr.ac.una.astroline.util.GsonUtil;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author takka_sama
 */
public class MantenimientoDeClientes {
 
    private ObservableList<Cliente> listaDeClientes;
    private static MantenimientoDeClientes instancia;
    private static final String ARCHIVO_JSON = "Clientes.json";
    
    public MantenimientoDeClientes(){
        listaDeClientes = FXCollections.observableArrayList();
    }

    public ObservableList<Cliente> getListaDeClientes() {
        return listaDeClientes;
    }
    
    public static MantenimientoDeClientes getInstancia(){
        
        if(instancia == null){
            instancia = new MantenimientoDeClientes();
            instancia.cargarListaDeClientes();
        }
        return instancia;
    }
    
    public boolean agregar(Cliente nuevoCliente){
    
        if(nuevoCliente == null || existe(nuevoCliente)) return false;
        
        listaDeClientes.add(nuevoCliente);
        
        GsonUtil.guardar(listaDeClientes, ARCHIVO_JSON);
        
        return true;
        
    }
    
    
    public boolean remover(Cliente clienteARemover){
        
        if(clienteARemover == null || !existe(clienteARemover)) return false;
        
        listaDeClientes.remove(clienteARemover);
        
        GsonUtil.guardar(listaDeClientes, ARCHIVO_JSON);
        
        return true;
        
    }

   public boolean existe(Cliente cliente){
       
       if(cliente == null || listaDeClientes == null) return false;
       
       for(Cliente c : listaDeClientes)
           if(c.getCedula().equals(cliente.getCedula())) return true;
       
       return false;
   }
   
  
   public void cargarListaDeClientes(){
       
       List<Cliente> lista = GsonUtil.leerLista(ARCHIVO_JSON, Cliente.class);
       if(lista == null)
           lista = new ArrayList<>();
       listaDeClientes.setAll(lista);
   
   }
   

}
