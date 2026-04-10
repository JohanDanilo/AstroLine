package cr.ac.una.astroline.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * Orquestador del sistema P2P.
 * Singleton. Se inicializa una vez desde App.java al arrancar.
 *
 * @author JohanDanilo
 */
public class SyncManager {

    private static final int POLL_INTERVAL_SECONDS = 15;
    private static SyncManager instancia;

    private final List<String> peers = new CopyOnWriteArrayList<>();
    private ScheduledExecutorService scheduler;

    private SyncManager() {}

    public static SyncManager getInstancia() {
        if (instancia == null) {
            synchronized (SyncManager.class) {
                if (instancia == null) instancia = new SyncManager();
            }
        }
        return instancia;
    }

    /**
     * Punto de entrada. Llamar desde App.java al arrancar.
     * 1. Inicia listener UDP
     * 2. Inicia servidor HTTP
     * 3. Descubre peers y sincroniza archivos
     * 4. Lanza polling periódico cada 15 segundos
     */
    public void iniciar() {
        System.out.println("[SyncManager] Iniciando sistema P2P...");

        NetworkPeer.startListening();
        SyncServer.start();

        List<String> discovered = NetworkPeer.discoverPeers();
        peers.addAll(discovered);

        if (peers.isEmpty()) {
            System.out.println("[SyncManager] Sin peers. Modo standalone.");
        } else {
            System.out.println("[SyncManager] " + peers.size() + " peer(s). Sincronizando...");
            peers.forEach(this::syncDesde);
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "astroline-sync-poller");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(
            this::pollPeers,
            POLL_INTERVAL_SECONDS, POLL_INTERVAL_SECONDS, TimeUnit.SECONDS
        );
        
            scheduler.scheduleAtFixedRate(
            this::redescubrirPeers,
            60, 60, TimeUnit.SECONDS
        );
    }

    /**
     * Propaga un archivo a todos los peers conocidos.
     * GsonUtil lo llama automáticamente después de cada guardarYPropagar().
     */
    public void propagar(String nombreArchivo) {
        if (peers.isEmpty()) return;
        Path filePath = Paths.get(GsonUtil.getDataDir(), nombreArchivo);
        if (!Files.exists(filePath)) return;
        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            for (String peer : peers) {
                boolean ok = SyncClient.postFile(peer, nombreArchivo, content);
                System.out.println("[SyncManager] " + nombreArchivo + " → " + peer + ": " + (ok ? "OK" : "FALLO"));
            }
        } catch (IOException e) {
            System.err.println("[SyncManager] Error propagando " + nombreArchivo + ": " + e.getMessage());
        }
    }
    
    private void redescubrirPeers() {
        List<String> descubiertos = NetworkPeer.discoverPeers();
        for (String ip : descubiertos) {
            registrarPeer(ip);
        }
    }

    // ── Polling periódico ────────────────────────────────────────────────────

    private void pollPeers() {
        for (String peer : peers) {
            try {
                for (Map<String, Object> info : SyncClient.getFileList(peer)) {
                    String name           = (String) info.get("name");
                    long   remoteModified = ((Number) info.get("lastModified")).longValue();
                    Path   localPath      = Paths.get(GsonUtil.getDataDir(), name);

                    if (deberiaActualizar(localPath, remoteModified)) {
                        System.out.println("[SyncManager] Archivo nuevo: " + name + " desde " + peer);
                        String content = SyncClient.getFile(peer, name);
                        if (content != null) escribirLocal(name, content, remoteModified);
                    }
                }
            } catch (Exception e) {
                System.err.println("[SyncManager] Fallo polling " + peer + ": " + e.getMessage());
            }
        }
    }

    // ── Sincronización inicial ───────────────────────────────────────────────

    private void syncDesde(String peer) {
        for (Map<String, Object> info : SyncClient.getFileList(peer)) {
            String name           = (String) info.get("name");
            long   remoteModified = ((Number) info.get("lastModified")).longValue();
            Path   localPath      = Paths.get(GsonUtil.getDataDir(), name);

            if (deberiaActualizar(localPath, remoteModified)) {
                String content = SyncClient.getFile(peer, name);
                if (content != null) escribirLocal(name, content, remoteModified);
            }
        }
    }

    /**
     * Decide si se debe descargar el archivo remoto.
     *
     * Reglas:
     * 1. Si no existe local → sí descargar
     * 2. Si el remoto es más nuevo → sí descargar
     * 3. Si el local está vacío ([], {}, "") aunque sea más nuevo → preferir remoto
     *    (evita que DataInitializer pise datos reales con un JSON recién creado)
     */
    private boolean deberiaActualizar(Path localPath, long remoteModified) {
        if (!Files.exists(localPath)) return true;

        long localModified = localPath.toFile().lastModified();
        if (remoteModified > localModified) return true;

        // El local es igual o más nuevo — verificar si está vacío
        try {
            String contenido = Files.readString(localPath, StandardCharsets.UTF_8).trim();
            boolean estaVacio = contenido.equals("[]")
                             || contenido.equals("{}")
                             || contenido.isEmpty();
            return estaVacio;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Escribe un archivo recibido de un peer en el disco local.
     * Preserva el timestamp remoto para evitar re-descargas por
     * diferencias de reloj entre máquinas.
     */
    private void escribirLocal(String nombre, String content, long remoteLastModified) {
        try {
            Path dataDir = Paths.get(GsonUtil.getDataDir());
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);

            synchronized (this) {
                Path destino = dataDir.resolve(nombre);
                Files.writeString(destino, content, StandardCharsets.UTF_8);
                // Preservar timestamp remoto — sin esto hay re-descargas infinitas
                // cuando los relojes de las máquinas no están perfectamente sincronizados
                Files.setLastModifiedTime(destino, FileTime.fromMillis(remoteLastModified));
            }

            System.out.println("[SyncManager] Guardado local: " + nombre);
            DataNotifier.notifyChange(nombre);

        } catch (IOException e) {
            System.err.println("[SyncManager] Error guardando " + nombre + ": " + e.getMessage());
        }
    }

    public void detener() {
        if (scheduler != null) scheduler.shutdown();
        SyncServer.stop();
        NetworkPeer.stop();
    }

    public void registrarPeer(String ip) {
        String ownIp = NetworkPeer.getOwnIp();
        if (!ip.equals(ownIp) && !peers.contains(ip)) {
            peers.add(ip);
            System.out.println("[SyncManager] Nuevo peer registrado: " + ip);
        }
    }
    
    public void propagarContenido(String contenido, String nombreArchivo) {
        if (peers.isEmpty()) return;
        for (String peer : peers) {
            boolean ok = SyncClient.postFile(peer, nombreArchivo, contenido);
            System.out.println("[SyncManager] " + nombreArchivo + " → " + peer + ": " + (ok ? "OK" : "FALLO"));
        }
    }
}