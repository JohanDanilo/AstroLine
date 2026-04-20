package cr.ac.una.astroline.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SyncClient {

    private static final int PORT = 8080;
    private static final int TIMEOUT_MS = 3000;
    private static final Gson gson = new Gson();

    private SyncClient() {
    }

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

    public static String getFile(String ip, String fileName) {
        try {
            return get("http://" + ip + ":" + PORT + "/file?name=" + fileName);
        } catch (Exception e) {
            System.err.println("[SyncClient] Error descargando " + fileName + " de " + ip + ": " + e.getMessage());
            return null;
        }
    }

    public static boolean postFile(String ip, String fileName, String content) {
        try {
            URL url = new URL("http://" + ip + ":" + PORT + "/file?name=" + fileName);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

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

    public static List<Map<String, Object>> getImageList(String ip) {
        try {
            String response = get("http://" + ip + ":" + PORT + "/images");
            return gson.fromJson(response,
                    TypeToken.getParameterized(List.class, Map.class).getType());
        } catch (Exception e) {
            System.err.println("[SyncClient] Error listando imágenes de " + ip + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public static byte[] getImage(String ip, String relPath) {
        try {
            String encoded = URLEncoder.encode(relPath, StandardCharsets.UTF_8);
            URL url = new URL("http://" + ip + ":" + PORT + "/image?name=" + encoded);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            if (conn.getResponseCode() != 200) {
                return null;
            }
            return conn.getInputStream().readAllBytes();
        } catch (Exception e) {
            System.err.println("[SyncClient] Error descargando imagen " + relPath + " de " + ip + ": " + e.getMessage());
            return null;
        }
    }

    public static boolean postImage(String ip, String relPath, byte[] content) {
        try {
            String encoded = URLEncoder.encode(relPath, StandardCharsets.UTF_8);
            URL url = new URL("http://" + ip + ":" + PORT + "/image?name=" + encoded);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/octet-stream");

            Path filePath = Paths.get(GsonUtil.getDataDir(), relPath.split("/"));
            if (Files.exists(filePath)) {
                conn.setRequestProperty("X-Last-Modified",
                        String.valueOf(Files.getLastModifiedTime(filePath).toMillis()));
            }

            try (OutputStream os = conn.getOutputStream()) {
                os.write(content);
            }
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            System.err.println("[SyncClient] Error enviando imagen " + relPath + " a " + ip + ": " + e.getMessage());
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
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString().trim();
        }
    }
}
