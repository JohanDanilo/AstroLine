package cr.ac.una.astroline.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.*;

/**
 * Cliente HTTP para hablar con los SyncServer de los peers.
 *
 * @author JohanDanilo
 */
public class SyncClient {

    private static final int    PORT       = 8080;
    private static final int    TIMEOUT_MS = 3000;
    private static final Gson   gson       = new Gson();

    private SyncClient() {}

    /**
     * Obtiene la lista de archivos disponibles en un peer.
     * @return lista de mapas con "name" y "lastModified"
     */
    public static List<Map<String, Object>> getFileList(String ip) {
        try {
            String response = get("http://" + ip + ":" + PORT + "/files");
            return gson.fromJson(response,
                TypeToken.getParameterized(List.class, Map.class).getType());
        } catch (Exception e) {
            System.err.println("[SyncClient] Error listando archivos de " + ip + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Descarga el contenido de un archivo desde un peer.
     * @return contenido como String, o null si falla
     */
    public static String getFile(String ip, String fileName) {
        try {
            return get("http://" + ip + ":" + PORT + "/file?name=" + fileName);
        } catch (Exception e) {
            System.err.println("[SyncClient] Error descargando " + fileName + " de " + ip + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Envía el contenido de un archivo a un peer.
     * @return true si el peer confirmó recepción
     */
    public static boolean postFile(String ip, String fileName, String content) {
        try {
            URL url = new URL("http://" + ip + ":" + PORT + "/file?name=" + fileName);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // Leer el timestamp actual del archivo y enviarlo como header
            Path filePath = Paths.get(GsonUtil.getDataDir(), fileName);
            if (Files.exists(filePath)) {
                long lastModified = Files.getLastModifiedTime(filePath).toMillis();
                conn.setRequestProperty("X-Last-Modified", String.valueOf(lastModified));
            }

            try (OutputStream os = conn.getOutputStream()) {
                os.write(content.getBytes(StandardCharsets.UTF_8));
            }
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            System.err.println("[SyncClient] Error enviando " + fileName + " a " + ip + ": " + e.getMessage());
            return false;
        }
    }

    private static String get(String urlStr) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
            return sb.toString().trim();
        }
    }
}