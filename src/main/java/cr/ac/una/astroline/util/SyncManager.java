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
     * 3. Descubre peers y sincroniza archivos e imágenes
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
            peers.forEach(peer -> {
                // Primero imágenes, luego JSON.
                // Así, cuando el JSON llegue y la UI se refresque,
                // los bytes de la imagen ya están en disco.
                syncImagenesDesde(peer);
                syncDesde(peer);
            });
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

    // ── Propagación hacia peers ──────────────────────────────────────────────

    /**
     * Propaga un archivo JSON a todos los peers conocidos.
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

    /**
     * Propaga una imagen a todos los peers conocidos.
     *
     * Se llama desde los controladores después de guardar un cliente o la empresa:
     *   SyncManager.getInstancia().propagarImagen("fotos/Cliente_123.png");
     *   SyncManager.getInstancia().propagarImagen("logoEmpresa/logo_empresa.png");
     *
     * @param relPath ruta relativa al dataDir con separador '/',
     *                ej. "fotos/Cliente_123.png".
     *                Si es una URL de recurso interno (file:, jar:) se ignora.
     */
    public void propagarImagen(String relPath) {
        if (peers.isEmpty() || relPath == null || relPath.isEmpty()) return;
        // Ignorar imágenes embebidas en el JAR
        if (relPath.startsWith("file:") || relPath.startsWith("jar:")) return;

        Path filePath = Paths.get(GsonUtil.getDataDir(), relPath.split("/"));
        if (!Files.exists(filePath)) {
            System.err.println("[SyncManager] Imagen no encontrada para propagar: " + relPath);
            return;
        }
        try {
            byte[] content = Files.readAllBytes(filePath);
            for (String peer : peers) {
                boolean ok = SyncClient.postImage(peer, relPath, content);
                System.out.println("[SyncManager] img:" + relPath + " → " + peer + ": " + (ok ? "OK" : "FALLO"));
            }
        } catch (IOException e) {
            System.err.println("[SyncManager] Error propagando imagen " + relPath + ": " + e.getMessage());
        }
    }

    public void propagarContenido(String contenido, String nombreArchivo) {
        if (peers.isEmpty()) return;
        for (String peer : peers) {
            boolean ok = SyncClient.postFile(peer, nombreArchivo, contenido);
            System.out.println("[SyncManager] " + nombreArchivo + " → " + peer + ": " + (ok ? "OK" : "FALLO"));
        }
    }

    // ── Polling periódico ────────────────────────────────────────────────────

    private void pollPeers() {
        for (String peer : peers) {
            try {
                // Imágenes primero → cuando la UI refresque por el JSON,
                // la foto ya existe localmente.
                syncImagenesDesde(peer);

                for (Map<String, Object> info : SyncClient.getFileList(peer)) {
                    String name           = (String) info.get("name");
                    long   remoteModified = ((Number) info.get("lastModified")).longValue();
                    Path   localPath      = Paths.get(GsonUtil.getDataDir(), name);

                    if (deberiaActualizar(localPath, remoteModified)) {
                        System.out.println("[SyncManager] Archivo nuevo: " + name + " desde " + peer);
                        String content = SyncClient.getFile(peer, name);
                        if (content != null) escribirJsonLocal(name, content, remoteModified);
                    }
                }
            } catch (Exception e) {
                System.err.println("[SyncManager] Fallo polling " + peer + ": " + e.getMessage());
            }
        }
    }

    private void redescubrirPeers() {
        List<String> descubiertos = NetworkPeer.discoverPeers();
        for (String ip : descubiertos) {
            registrarPeer(ip);
        }
    }

    // ── Sincronización inicial ───────────────────────────────────────────────

    /** Sincroniza archivos JSON desde un peer. */
    private void syncDesde(String peer) {
        for (Map<String, Object> info : SyncClient.getFileList(peer)) {
            String name           = (String) info.get("name");
            long   remoteModified = ((Number) info.get("lastModified")).longValue();
            Path   localPath      = Paths.get(GsonUtil.getDataDir(), name);

            if (deberiaActualizar(localPath, remoteModified)) {
                String content = SyncClient.getFile(peer, name);
                if (content != null) escribirJsonLocal(name, content, remoteModified);
            }
        }
    }

    /**
     * Sincroniza imágenes desde un peer.
     * Compara por lastModified; si el remoto es más nuevo (o no existe local),
     * descarga los bytes y los escribe en el subdirectorio correspondiente.
     */
    private void syncImagenesDesde(String peer) {
        for (Map<String, Object> info : SyncClient.getImageList(peer)) {
            String relPath        = (String) info.get("name"); // "fotos/Cliente_123.png"
            long   remoteModified = ((Number) info.get("lastModified")).longValue();

            // Construir ruta local segura segmento a segmento
            String[] segmentos = relPath.split("/");
            Path localPath = Paths.get(GsonUtil.getDataDir(), segmentos);

            boolean necesitaActualizar = !Files.exists(localPath)
                || localPath.toFile().lastModified() < remoteModified;

            if (necesitaActualizar) {
                System.out.println("[SyncManager] Imagen nueva: " + relPath + " desde " + peer);
                byte[] bytes = SyncClient.getImage(peer, relPath);
                if (bytes != null) escribirImagenLocal(relPath, bytes, remoteModified);
            }
        }
    }

    // ── Escritura local ──────────────────────────────────────────────────────

    /**
     * Escribe un JSON recibido de un peer en disco y notifica a la UI.
     */
    private void escribirJsonLocal(String nombre, String content, long remoteLastModified) {
        try {
            Path dataDir = Paths.get(GsonUtil.getDataDir());
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);

            synchronized (this) {
                Path destino = dataDir.resolve(nombre);
                Files.writeString(destino, content, StandardCharsets.UTF_8);
                Files.setLastModifiedTime(destino, FileTime.fromMillis(remoteLastModified));
            }

            System.out.println("[SyncManager] JSON guardado: " + nombre);
            DataNotifier.notifyChange(nombre);

        } catch (IOException e) {
            System.err.println("[SyncManager] Error guardando JSON " + nombre + ": " + e.getMessage());
        }
    }

    /**
     * Escribe los bytes de una imagen recibida de un peer.
     * Preserva el timestamp remoto para evitar re-descargas infinitas.
     * Notifica a DataNotifier con la ruta relativa para que los controladores
     * suscritos puedan refrescar la UI en tiempo real.
     *
     * @param relPath ruta relativa al dataDir, ej. "fotos/Cliente_123.png"
     */
    private void escribirImagenLocal(String relPath, byte[] content, long remoteLastModified) {
        try {
            String[] segmentos = relPath.split("/");
            Path destino = Paths.get(GsonUtil.getDataDir(), segmentos);
            Files.createDirectories(destino.getParent());

            synchronized (this) {
                Files.write(destino, content);
                Files.setLastModifiedTime(destino, FileTime.fromMillis(remoteLastModified));
            }

            System.out.println("[SyncManager] Imagen guardada: " + relPath);
            // Notificar a los controladores suscritos para que refresquen la UI
            DataNotifier.notifyChange(relPath);

        } catch (IOException e) {
            System.err.println("[SyncManager] Error guardando imagen " + relPath + ": " + e.getMessage());
        }
    }

    // ── Lógica de decisión ───────────────────────────────────────────────────

    /**
     * Decide si se debe descargar el archivo JSON remoto.
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

        try {
            String contenido = Files.readString(localPath, StandardCharsets.UTF_8).trim();
            return contenido.equals("[]") || contenido.equals("{}") || contenido.isEmpty();
        } catch (IOException e) {
            return false;
        }
    }

    // ── Gestión de peers ─────────────────────────────────────────────────────

    public void registrarPeer(String ip) {
        String ownIp = NetworkPeer.getOwnIp();
        if (!ip.equals(ownIp) && !peers.contains(ip)) {
            peers.add(ip);
            System.out.println("[SyncManager] Nuevo peer registrado: " + ip);
        }
    }

    public void detener() {
        if (scheduler != null) scheduler.shutdown();
        SyncServer.stop();
        NetworkPeer.stop();
    }
}