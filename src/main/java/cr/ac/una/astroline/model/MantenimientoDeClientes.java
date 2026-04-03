
package cr.ac.una.astroline.model;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author takka_sama
 */
public class MantenimientoDeClientes {
 
    private List<Cliente> listaDeClientes;
    
    public MantenimientoDeClientes(){
        listaDeClientes = new ArrayList<>();
    }
    
    
    public void agregar(Cliente nuevoCliente){
        
        if (listaDeClientes.isEmpty()) listaDeClientes = new ArrayList <>();
        
        listaDeClientes.add(nuevoCliente);
        
    }
    
    
    public void remover(Cliente clienteARemover){
        
        listaDeClientes.remove(clienteARemover);
        
    }

   public boolean existe(Cliente cliente){
       
       if(cliente == null || listaDeClientes == null) return false;
       
       for(Cliente c : listaDeClientes)
           if(c.getCedula().equals(cliente.getCedula())) return true;
       
       return false;
   }
   
    private boolean encontrarClientePorNombre(String nombreDeCliente){
       
       if(listaDeClientes.isEmpty()) return listaDeClientes.isEmpty();
       
       for (int i = listaDeClientes.size(); i >= 0; i-- ){
           if(listaDeClientes.get(i).getNombre().equals(nombreDeCliente)){
               return true;
           }
           
       }
       return false;
   }
  
   
   
}
