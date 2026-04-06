package cr.ac.una.astroline.service;

import cr.ac.una.astroline.model.Cliente;
import cr.ac.una.astroline.model.ClienteDTO;
import cr.ac.una.astroline.util.GsonUtil;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Servicio singleton para la gestión persistente de clientes.
 */
public class ClienteService {

    private final ObservableList<Cliente> listaDeClientes;
    private static ClienteService instancia;

    private static final String ARCHIVO_JSON = "clientes.json";
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private ClienteService() {
        listaDeClientes = FXCollections.observableArrayList();
    }

    public static ClienteService getInstancia() {
        if (instancia == null) {
            instancia = new ClienteService();
            instancia.cargarClientes();
        }
        return instancia;
    }

    public ObservableList<Cliente> getListaDeClientes() {
        return listaDeClientes;
    }

    // ── Conversiones DTO ↔ Modelo ────────────────────────────────────────────

    /**
     * Carga los datos de un Cliente existente en un DTO para edición en UI.
     */
    public void cargarEnDTO(Cliente cliente, ClienteDTO dto) {
        if (cliente == null || dto == null) return;
        dto.setCedula(cliente.getCedula());
        dto.setNombre(cliente.getNombre());
        dto.setApellidos(cliente.getApellidos());
        dto.setTelefono(cliente.getTelefono());
        dto.setCorreo(cliente.getCorreo());
        dto.setFotoPath(cliente.getFotoPath());
        if (cliente.getFechaNacimiento() != null && !cliente.getFechaNacimiento().isEmpty()) {
            dto.setFechaNacimiento(
                java.time.LocalDate.parse(cliente.getFechaNacimiento(), FORMATO_FECHA)
            );
        }
    }

    /**
     * Construye un Cliente a partir de los datos de un DTO de formulario.
     */
    public Cliente dtoACliente(ClienteDTO dto) {
        if (dto == null) return null;
        String fecha = dto.getFechaNacimiento() != null
                ? dto.getFechaNacimiento().format(FORMATO_FECHA)
                : "";
        return new Cliente(
                dto.getCedula(),
                dto.getNombre(),
                dto.getApellidos(),
                dto.getTelefono(),
                dto.getCorreo(),
                dto.getFotoPath(),
                fecha
        );
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    public Cliente buscarPorCedula(String cedula) {
        if (cedula == null) return null;
        for (Cliente c : listaDeClientes) {
            if (c.getCedula().equals(cedula)) return c;
        }
        return null;
    }

    public boolean existe(Cliente cliente) {
        if (cliente == null) return false;
        return buscarPorCedula(cliente.getCedula()) != null;
    }

    public boolean agregar(Cliente nuevoCliente) {
        if (nuevoCliente == null || existe(nuevoCliente)) return false;
        listaDeClientes.add(nuevoCliente);
        GsonUtil.guardar(listaDeClientes, ARCHIVO_JSON);
        return true;
    }

    public boolean remover(Cliente clienteARemover) {
        if (clienteARemover == null || !existe(clienteARemover)) return false;
        listaDeClientes.remove(clienteARemover);
        GsonUtil.guardar(listaDeClientes, ARCHIVO_JSON);
        return true;
    }

    public boolean actualizar(Cliente clienteActualizado) {
        if (clienteActualizado == null) return false;
        for (int i = 0; i < listaDeClientes.size(); i++) {
            if (listaDeClientes.get(i).getCedula().equals(clienteActualizado.getCedula())) {
                listaDeClientes.set(i, clienteActualizado);
                GsonUtil.guardar(listaDeClientes, ARCHIVO_JSON);
                return true;
            }
        }
        return false;
    }

    private void cargarClientes() {
        List<Cliente> lista = GsonUtil.leerLista(ARCHIVO_JSON, Cliente.class);
        if (lista == null) lista = new ArrayList<>();
        listaDeClientes.setAll(lista);
    }
}