package cr.ac.una.astroline.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PathManager {

    private static final String PROPERTIES_FILE = "properties.json";
    private static final String CONFIG_FILE     = "configuracion.json";
    private static final String KEY_RUTA_DATOS  = "rutaDatos";

    /** Raíz del proyecto / carpeta del JAR */
    public static Path getPropertiesPath() {
        return Path.of(PROPERTIES_FILE);
    }

    /** Raíz del proyecto / carpeta del JAR */
    public static Path getGlobalConfigPath() {
        return Path.of(CONFIG_FILE);
    }

    public static Path getDataPath() {
        Path propertiesPath = getPropertiesPath();
        if (Files.exists(propertiesPath)) {
            try {
                String json = Files.readString(propertiesPath);
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                if (obj.has(KEY_RUTA_DATOS) && !obj.get(KEY_RUTA_DATOS).isJsonNull()) {
                    return Path.of(obj.get(KEY_RUTA_DATOS).getAsString());
                }
            } catch (Exception e) {
                System.err.println("[PathManager] Error leyendo properties.json: " + e.getMessage());
            }
        }
        return Path.of(System.getProperty("user.home"), "Documents", "AstroLine");
    }

    
    public String getRutaDatos(){
        return KEY_RUTA_DATOS;
    }
    /**
     * Persiste la nueva ruta de datos en properties.json.
     * Preserva cualquier otra clave existente.
     */
    public static void setDataPath(Path nuevaRuta) throws IOException {
        Path propertiesPath = getPropertiesPath();

        JsonObject obj = new JsonObject();
        if (Files.exists(propertiesPath)) {
            try {
                obj = JsonParser.parseString(Files.readString(propertiesPath)).getAsJsonObject();
            } catch (Exception ignored) {}
        }
        obj.addProperty(KEY_RUTA_DATOS, nuevaRuta.toString());
        Files.writeString(propertiesPath, GsonUtil.getGson().toJson(obj));
    }
}