package cr.ac.una.astroline.util;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servidor HTTP liviano para sincronización de archivos JSON entre peers.
 * Tres endpoints: listar archivos, descargar archivo, recibir archivo.
 *
 * @author JohanDanilo
 */
public class SyncServer {

    private static final int PORT = 8080;
    private static HttpServer server;

    private SyncServer() {}

    public static void start() {
        try {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/files", new FilesListHandler());
        server.createContext("/file",  new FileHandler());

        // Executor con threads daemon para que no bloquee el cierre
        ExecutorService executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "astroline-http-worker");
            t.setDaemon(true); // ← clave
            return t;
        });
        server.setExecutor(executor);
        server.start();
    } catch (IOException e) {
            System.err.println("[SyncServer] No se pudo iniciar: " + e.getMessage());
        }
    }

    public static void stop() {
        if (server != null) server.stop(0);
    }

    // ─── GET /files ─────────────────────────────────────────────────────────────
    // Responde con JSON: [{"name":"clientes.json","lastModified":1234567890}, ...]

    static class FilesListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"GET".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(405, -1); return; }

            StringBuilder json = new StringBuilder("[");
            Path dataDir = Paths.get(GsonUtil.getDataDir());

            if (Files.exists(dataDir)) {
                File[] files = dataDir.toFile().listFiles((d, n) -> n.endsWith(".json"));
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        if (i > 0) json.append(",");
                        json.append(String.format(
                            "{\"name\":\"%s\",\"lastModified\":%d}",
                            files[i].getName(), files[i].lastModified()
                        ));
                    }
                }
            }
            json.append("]");

            byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().set("Content-Type", "application/json");
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
        }
    }

    // ─── GET /file?name=x  →  devuelve contenido ────────────────────────────────
    // ─── POST /file?name=x →  guarda contenido  ────────────────────────────────

    static class FileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String fileName = extractParam(ex.getRequestURI().getQuery(), "name");
            if (fileName == null || fileName.isEmpty()) { ex.sendResponseHeaders(400, -1); return; }
            fileName = Paths.get(fileName).getFileName().toString(); // bloquea path traversal

            if ("GET".equals(ex.getRequestMethod())) {
                Path path = Paths.get(GsonUtil.getDataDir(), fileName);
                if (!Files.exists(path)) { ex.sendResponseHeaders(404, -1); return; }
                byte[] content = Files.readAllBytes(path);
                ex.getResponseHeaders().set("Content-Type", "application/json");
                ex.sendResponseHeaders(200, content.length);
                try (OutputStream os = ex.getResponseBody()) { os.write(content); }

            } else if ("POST".equals(ex.getRequestMethod())) {
                // Registrar al peer que nos envió datos
                String senderIp = ex.getRemoteAddress().getAddress().getHostAddress();
                SyncManager.getInstancia().registrarPeer(senderIp);

                byte[] content = ex.getRequestBody().readAllBytes();
                Path dataDir = Paths.get(GsonUtil.getDataDir());
                if (!Files.exists(dataDir)) Files.createDirectories(dataDir);

                synchronized (SyncManager.getInstancia()) {
                    Path destino = dataDir.resolve(fileName);
                    Files.write(destino, content);

                    // Preservar el timestamp original del peer que envió el archivo
                    String lmHeader = ex.getRequestHeaders().getFirst("X-Last-Modified");
                    if (lmHeader != null) {
                        try {
                            Files.setLastModifiedTime(destino,
                                FileTime.fromMillis(Long.parseLong(lmHeader)));
                        } catch (NumberFormatException ignored) {}
                    }
                }

                ex.sendResponseHeaders(200, -1);
                DataNotifier.notifyChange(fileName);
            } else {
                ex.sendResponseHeaders(405, -1);
            }
        }

        private String extractParam(String query, String param) {
            if (query == null) return null;
            for (String part : query.split("&")) {
                String[] kv = part.split("=", 2);
                if (kv.length == 2 && param.equals(kv[0]))
                    return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
            return null;
        }
    }
}