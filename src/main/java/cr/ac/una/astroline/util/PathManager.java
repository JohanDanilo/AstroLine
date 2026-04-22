package cr.ac.una.astroline.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PathManager {

    private static final String APP_FOLDER  = "AstroLine";
    private static final String CONFIG_FILE = "properties.json";
    private static final String KEY_RUTA_DATOS = "rutaDatos";

    /** Carpeta base: ~/Documents/AstroLine  (Windows, Linux y Mac) */
    private static Path getBaseDir() {
        return Path.of(System.getProperty("user.home"), "Documents", APP_FOLDER);
    }

    /** Archivo de configuración global: ~/Documents/AstroLine/properties.json */
    public static Path getGlobalConfigPath() {
        return getBaseDir().resolve(CONFIG_FILE);
    }

    /** Ruta donde viven los datos (lee desde el config global). */
    public static Path getDataPath() {
        Path configPath = getGlobalConfigPath();
        if (Files.exists(configPath)) {
            try {
                String json = Files.readString(configPath);
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                if (obj.has(KEY_RUTA_DATOS)) {
                    return Path.of(obj.get(KEY_RUTA_DATOS).getAsString());
                }
            } catch (Exception e) {
                System.err.println("[PathManager] Error leyendo config global: " + e.getMessage());
            }
        }
        // Primera vez: default = ~/Documents/AstroLine/data/
        return getBaseDir().resolve("data");
    }

    /** Persiste una nueva ruta de datos en el config global. */
    public static void setDataPath(Path nuevaRuta) throws IOException {
        Path configPath = getGlobalConfigPath();
        Files.createDirectories(configPath.getParent());

        JsonObject obj = new JsonObject();
        if (Files.exists(configPath)) {
            try {
                String existing = Files.readString(configPath);
                obj = JsonParser.parseString(existing).getAsJsonObject();
            } catch (Exception ignored) {}
        }
        obj.addProperty(KEY_RUTA_DATOS, nuevaRuta.toString());
        // Files.writeString(configPath, GsonUtil.getGson().toJson(obj));
    }
}