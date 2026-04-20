package cr.ac.una.astroline.service;

import cr.ac.una.astroline.model.Cliente;
import cr.ac.una.astroline.model.ClienteDTO;
import cr.ac.una.astroline.util.DataNotifier;
import cr.ac.una.astroline.util.GsonUtil;
import cr.ac.una.astroline.util.SyncManager;

import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ClienteService implements DataNotifier.Listener {

    private final ObservableList<Cliente> listaDeClientes;
    private static ClienteService instancia;

    private static final String ARCHIVO_JSON = "clientes.json";
    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd-MM-yyyy");

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
        return listaDeClientes.filtered(c -> !c.isEliminado());
    }

    public void cargarEnDTO(Cliente cliente, ClienteDTO dto) {
        if (cliente == null || dto == null) {
            return;
        }

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
        if (dto == null) {
            return null;
        }

        String fecha = dto.getFechaNacimiento() != null ? dto.getFechaNacimiento().format(FORMATO_FECHA) : "";

        return new Cliente(dto.getCedula(), dto.getNombre(), dto.getApellidos(), dto.getTelefono(), dto.getCorreo(), dto.getFotoPath(), fecha);
    }

    public Cliente buscarPorCedula(String cedula) {
        if (cedula == null) {
            return null;
        }
        for (Cliente c : listaDeClientes) {
            if (c.getCedula().equals(cedula)) {
                return c;
            }
        }
        return null;
    }

    public boolean existe(Cliente cliente) {
        if (cliente == null) {
            return false;
        }
        Cliente encontrado = buscarPorCedula(cliente.getCedula());
        return encontrado != null && !encontrado.isEliminado();
    }

    public boolean agregar(Cliente nuevoCliente) {
        if (nuevoCliente == null || existe(nuevoCliente)) {
            return false;
        }
        nuevoCliente.setLastModified(System.currentTimeMillis());
        listaDeClientes.add(nuevoCliente);
        GsonUtil.guardarYPropagar(listaDeClientes, ARCHIVO_JSON);
        return true;
    }

    public boolean remover(Cliente clienteARemover) {
        if (clienteARemover == null) {
            return false;
        }
        for (int i = 0; i < listaDeClientes.size(); i++) {
            Cliente c = listaDeClientes.get(i);
            if (c.getCedula().equals(clienteARemover.getCedula())) {

                c.setEliminado(true);
                c.setLastModified(System.currentTimeMillis());
                listaDeClientes.set(i, c);
                SyncManager.getInstancia().propagarContenido(GsonUtil.toJson(listaDeClientes), ARCHIVO_JSON);

                listaDeClientes.remove(i);
                GsonUtil.guardar(listaDeClientes, ARCHIVO_JSON);
                return true;
            }
        }
        return false;
    }

    public boolean actualizar(Cliente clienteActualizado) {
        if (clienteActualizado == null) {
            return false;
        }
        for (int i = 0; i < listaDeClientes.size(); i++) {
            if (listaDeClientes.get(i).getCedula().equals(clienteActualizado.getCedula())) {
                clienteActualizado.setLastModified(System.currentTimeMillis());
                listaDeClientes.set(i, clienteActualizado);
                GsonUtil.guardarYPropagar(listaDeClientes, ARCHIVO_JSON);
                return true;
            }
        }
        return false;
    }

    private void cargarClientes() {
        List<Cliente> lista = GsonUtil.leerLista(ARCHIVO_JSON, Cliente.class);
        listaDeClientes.setAll(lista);
    }

    @Override
    public void onDataChanged(String fileName) {
        if (!ARCHIVO_JSON.equals(fileName)) {
            return;
        }

        System.out.println("[ClienteService] Detectado cambio externo, sincronizando...");

        Platform.runLater(() -> {
            List<Cliente> nuevos = GsonUtil.leerLista(ARCHIVO_JSON, Cliente.class);
            if (nuevos != null) {
                mergeClientes(nuevos);
            }
        });
    }

    private void mergeClientes(List<Cliente> remotos) {
        Map<String, Cliente> mapa = new LinkedHashMap<>();
        for (Cliente c : listaDeClientes) {
            mapa.put(c.getCedula(), c);
        }

        boolean hayTombstones = false;
        for (Cliente r : remotos) {
            if (r.isEliminado()) {
                mapa.remove(r.getCedula());
                hayTombstones = true;
            } else {
                Cliente local = mapa.get(r.getCedula());
                if (local == null || r.getLastModified() >= local.getLastModified()) {
                    mapa.put(r.getCedula(), r);
                }
            }
        }

        listaDeClientes.setAll(mapa.values());

        if (hayTombstones) {
            GsonUtil.guardar(listaDeClientes, ARCHIVO_JSON);
        }
    }
}
