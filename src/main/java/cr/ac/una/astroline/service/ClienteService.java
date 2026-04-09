package cr.ac.una.astroline.service;

import cr.ac.una.astroline.model.Cliente;
import cr.ac.una.astroline.model.ClienteDTO;
import cr.ac.una.astroline.util.DataNotifier;
import cr.ac.una.astroline.util.GsonUtil;

import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Servicio singleton para la gestión persistente de clientes.
 * Reactivo y sincronizado entre peers via DataNotifier.
 *
 * @author JohanDanilo
 */
public class ClienteService implements DataNotifier.Listener {

    private final ObservableList<Cliente> listaDeClientes;
    private static ClienteService instancia;

    private static final String ARCHIVO_JSON = "clientes.json";
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private ClienteService() {
        listaDeClientes = FXCollections.observableArrayList();
        cargarClientes();
        DataNotifier.subscribe(this);
    }

    public static ClienteService getInstancia() {
        if (instancia == null) {
            instancia = new ClienteService();
        }
        return instancia;
    }

    public ObservableList<Cliente> getListaDeClientes() {
        return listaDeClientes;
    }

    // ── Conversiones DTO ↔ Modelo ────────────────────────────────────────────

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
        GsonUtil.guardarYPropagar(listaDeClientes, ARCHIVO_JSON);
        return true;
    }

    public boolean remover(Cliente clienteARemover) {
        if (clienteARemover == null || !existe(clienteARemover)) return false;
        listaDeClientes.remove(clienteARemover);
        GsonUtil.guardarYPropagar(listaDeClientes, ARCHIVO_JSON);
        return true;
    }

    public boolean actualizar(Cliente clienteActualizado) {
        if (clienteActualizado == null) return false;
        for (int i = 0; i < listaDeClientes.size(); i++) {
            if (listaDeClientes.get(i).getCedula().equals(clienteActualizado.getCedula())) {
                listaDeClientes.set(i, clienteActualizado);
                GsonUtil.guardarYPropagar(listaDeClientes, ARCHIVO_JSON);
                return true;
            }
        }
        return false;
    }

    // ── Carga inicial ────────────────────────────────────────────────────────

    private void cargarClientes() {
        List<Cliente> lista = GsonUtil.leerLista(ARCHIVO_JSON, Cliente.class);
        listaDeClientes.setAll(lista);
    }

    // ── Reactividad (cambios externos desde peers) ───────────────────────────

    @Override
    public void onDataChanged(String fileName) {
        if (!ARCHIVO_JSON.equals(fileName)) return;

        System.out.println("[ClienteService] Detectado cambio externo, sincronizando...");

        // CRÍTICO: setAll() modifica un ObservableList ligado a la UI.
        // DataNotifier lo dispara desde un hilo de red (SyncServer HTTP thread).
        // JavaFX requiere que toda modificación de UI ocurra en su propio hilo.
        // Sin Platform.runLater(), el cambio llega al disco pero la vista no se actualiza.
        Platform.runLater(() -> {
            List<Cliente> nuevos = GsonUtil.leerLista(ARCHIVO_JSON, Cliente.class);
            if (nuevos != null) mergeClientes(nuevos);
        });
    }

    /**
     * Merge basado en cédula.
     * Usa el archivo remoto como fuente de verdad — incluye eliminaciones.
     * El mapa se construye desde el remoto, no desde el local, para que
     * las eliminaciones hechas en otro peer se reflejen aquí.
     */
    private void mergeClientes(List<Cliente> nuevos) {
        // Usar LinkedHashMap para mantener orden de inserción
        Map<String, Cliente> mapa = new LinkedHashMap<>();
        for (Cliente c : nuevos) {
            mapa.put(c.getCedula(), c);
        }
        listaDeClientes.setAll(mapa.values());
    }
}