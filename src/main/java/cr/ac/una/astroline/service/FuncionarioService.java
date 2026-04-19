package cr.ac.una.astroline.service;

import cr.ac.una.astroline.model.Funcionario;
import cr.ac.una.astroline.model.FuncionarioDTO;
import cr.ac.una.astroline.util.DataNotifier;
import cr.ac.una.astroline.util.GsonUtil;
import cr.ac.una.astroline.util.SyncManager;

import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Servicio singleton para la gestión persistente de funcionarios.
 * Reactivo y sincronizado entre peers via DataNotifier.
 *
 * @author JohanDanilo
 */
public class FuncionarioService implements DataNotifier.Listener {

    private final ObservableList<Funcionario> listaDeFuncionarios;
    private static FuncionarioService instancia;

    private static final String ARCHIVO_JSON = "funcionarios.json";
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private FuncionarioService() {
        listaDeFuncionarios = FXCollections.observableArrayList();
        cargarFuncionarios();
        DataNotifier.subscribe(this);
    }

    public static FuncionarioService getInstancia() {
        if (instancia == null) {
            instancia = new FuncionarioService();
        }
        return instancia;
    }

    public ObservableList<Funcionario> getListaDeFuncionarios() {
        return listaDeFuncionarios.filtered(c -> !c.isEliminado());
    }

    // ── Conversiones DTO ↔ Modelo ────────────────────────────────────────────

    public void cargarEnDTO(Funcionario funcionario, FuncionarioDTO dto) {
        if (funcionario == null || dto == null) return;

        dto.setCedula(funcionario.getCedula());
        dto.setNombre(funcionario.getNombre());
        dto.setApellidos(funcionario.getApellidos());
        dto.setTelefono(funcionario.getTelefono());
        dto.setCorreo(funcionario.getCorreo());
        dto.setFotoPath(funcionario.getFotoPath());

        if (funcionario.getFechaNacimiento() != null && !funcionario.getFechaNacimiento().isEmpty()) {
            dto.setFechaNacimiento(
                java.time.LocalDate.parse(funcionario.getFechaNacimiento(), FORMATO_FECHA)
            );
        }
        
        dto.setUsername(funcionario.getUsername());
        dto.setPassword(funcionario.getPassword());
        dto.setEsAdmin(funcionario.esAdmin());
    }

    public Funcionario dtoAFuncionario(FuncionarioDTO dto) {
        if (dto == null) return null;

        String fecha = dto.getFechaNacimiento() != null
                ? dto.getFechaNacimiento().format(FORMATO_FECHA)
                : "";

        Funcionario f = new Funcionario(
                dto.getCedula(),
                dto.getNombre(),
                dto.getApellidos(),
                dto.getTelefono(),
                dto.getCorreo(),
                dto.getFotoPath(),
                fecha
        );
        
        f.setUsername(dto.getUsername());
        f.setPassword(dto.getPassword());
        f.setAdmin(dto.esAdmin());

        return f;
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    public Funcionario buscarPorCedula(String cedula) {
        if (cedula == null) return null;
        for (Funcionario f : listaDeFuncionarios) {
            if (f.getCedula().equals(cedula)) return f;
        }
        return null;
    }

    public boolean existe(Funcionario funcionario) {
        if (funcionario == null) return false;
        Funcionario encontrado = buscarPorCedula(funcionario.getCedula());
        return encontrado != null && !encontrado.isEliminado();
    }

    public boolean agregar(Funcionario nuevoFuncionario) {
        if (nuevoFuncionario == null || existe(nuevoFuncionario)) return false;
        nuevoFuncionario.setLastModified(System.currentTimeMillis());
        listaDeFuncionarios.add(nuevoFuncionario);
        GsonUtil.guardarYPropagar(listaDeFuncionarios, ARCHIVO_JSON);
        return true;
    }

    public boolean remover(Funcionario funcionarioARemover) {
        if (funcionarioARemover == null) return false;
        for (int i = 0; i < listaDeFuncionarios.size(); i++) {
            Funcionario f = listaDeFuncionarios.get(i);
            if (f.getCedula().equals(funcionarioARemover.getCedula())) {

                // Construir tombstone en memoria y propagarlo sin tocar el disco
                f.setEliminado(true);
                f.setLastModified(System.currentTimeMillis());
                listaDeFuncionarios.set(i, f);
                SyncManager.getInstancia().propagarContenido(
                    GsonUtil.toJson(listaDeFuncionarios), ARCHIVO_JSON);

                // Eliminar localmente y guardar limpio — una sola escritura a disco
                listaDeFuncionarios.remove(i);
                GsonUtil.guardar(listaDeFuncionarios, ARCHIVO_JSON);
                return true;
            }
        }
        return false;
    }

    public boolean actualizar(Funcionario funcionarioActualizado) {
        if (funcionarioActualizado == null) return false;
        for (int i = 0; i < listaDeFuncionarios.size(); i++) {
            if (listaDeFuncionarios.get(i).getCedula().equals(funcionarioActualizado.getCedula())) {
                funcionarioActualizado.setLastModified(System.currentTimeMillis());
                listaDeFuncionarios.set(i, funcionarioActualizado);
                GsonUtil.guardarYPropagar(listaDeFuncionarios, ARCHIVO_JSON);
                return true;
            }
        }
        return false;
    }

    // ── Carga inicial ────────────────────────────────────────────────────────

    private void cargarFuncionarios() {
        List<Funcionario> lista = GsonUtil.leerLista(ARCHIVO_JSON, Funcionario.class);
        listaDeFuncionarios.setAll(lista);
    }

    // ── Reactividad (cambios externos desde peers) ───────────────────────────

    @Override
    public void onDataChanged(String fileName) {
        if (!ARCHIVO_JSON.equals(fileName)) return;

        System.out.println("[FuncionarioService] Detectado cambio externo, sincronizando...");

        // CRÍTICO: setAll() modifica un ObservableList ligado a la UI.
        // DataNotifier lo dispara desde un hilo de red (SyncServer HTTP thread).
        // JavaFX requiere que toda modificación de UI ocurra en su propio hilo.
        // Sin Platform.runLater(), el cambio llega al disco pero la vista no se actualiza.
        Platform.runLater(() -> {
            List<Funcionario> nuevos = GsonUtil.leerLista(ARCHIVO_JSON, Funcionario.class);
            if (nuevos != null) mergeFuncionarios(nuevos);
        });
    }

    /**
    * Merge basado en cédula y lastModified.
    * Gana siempre el registro más reciente.
    * Las eliminaciones viajan como tombstones (eliminado=true),
    * por lo que nunca se pierden en un merge.
    */
    private void mergeFuncionarios(List<Funcionario> remotos) {
        Map<String, Funcionario> mapa = new LinkedHashMap<>();
        for (Funcionario f : listaDeFuncionarios) mapa.put(f.getCedula(), f);

        boolean hayTombstones = false;
        for (Funcionario r : remotos) {
            if (r.isEliminado()) {
                mapa.remove(r.getCedula());
                hayTombstones = true;
            } else {
                Funcionario local = mapa.get(r.getCedula());
                if (local == null || r.getLastModified() >= local.getLastModified()) {
                    mapa.put(r.getCedula(), r);
                }
            }
        }

        listaDeFuncionarios.setAll(mapa.values());

        // Solo guarda si había tombstones — los limpia del disco local
        // Sin esta condición se actualizaría el timestamp innecesariamente y volvería el loop
        if (hayTombstones) {
            GsonUtil.guardar(listaDeFuncionarios, ARCHIVO_JSON);
        }
    }
    
    /**
    * Verifica credenciales y retorna el Funcionario autenticado,
    * o null si el username no existe, la cuenta está eliminada,
    * o la contraseña no coincide.
    *
    * NOTA: en producción la contraseña debería almacenarse hasheada
    * (ej. BCrypt). Aquí se compara en texto plano para esta etapa del proyecto.
    */
   public Funcionario login(String username, String password) {
       if (username == null || password == null) return null;

       for (Funcionario f : listaDeFuncionarios) {
           if (!f.isEliminado()
                   && username.equals(f.getUsername())
                   && password.equals(f.getPassword())) {
               return f;
           }
       }
       return null;
   }

}