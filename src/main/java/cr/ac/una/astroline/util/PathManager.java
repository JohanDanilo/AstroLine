package cr.ac.una.astroline.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Centraliza la resolución de rutas del sistema.
 *
 * Jerarquía de archivos:
 *   ~/.astroline/properties.json          → bootstrap local (una copia por máquina)
 *   <rutaDatos>/configuracion.json        → config de estación/sucursal (compartida)
 *   <rutaDatos>/*.json                    → datos de la aplicación (compartidos)
 *
 * @author JohanDanilo
 */
public class PathManager {

    private static final String PROPERTIES_FILE = "properties.json";
    private static final String CONFIG_FILE      = "configuracion.json";
    private static final String KEY_RUTA_DATOS   = "rutaDatos";

    /**
     * Ruta fija del bootstrap: ~/.astroline/properties.json
     * Calculable sin leer ningún otro archivo → no hay dependencia circular.
     * Única por máquina → todos los módulos convergen al mismo archivo.
     */
    public static Path getPropertiesPath() {
        return Path.of(System.getProperty("user.home"), ".astroline", PROPERTIES_FILE);
    }

    /**
     * Ruta de configuracion.json dentro del data path.
     * Compartida entre todos los módulos que apunten al mismo data path.
     */
    public static Path getGlobalConfigPath() {
        return getDataPath().resolve(CONFIG_FILE);
    }

    /**
     * Determina la carpeta de datos leyendo properties.json.
     * Fallback: ~/Documents/AstroLine
     */
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

    /**
     * Persiste la nueva ruta de datos en properties.json.
     * Preserva cualquier otra clave existente.
     * Al escribir en ~/.astroline/properties.json, el cambio es inmediatamente
     * visible para todos los módulos en esta máquina.
     */
    public static void setDataPath(Path nuevaRuta) throws IOException {
        Path propertiesPath = getPropertiesPath();
        Files.createDirectories(propertiesPath.getParent());

        JsonObject obj = new JsonObject();
        if (Files.exists(propertiesPath)) {
            try {
                obj = JsonParser.parseString(Files.readString(propertiesPath)).getAsJsonObject();
            } catch (Exception ignored) {}
        }
        obj.addProperty(KEY_RUTA_DATOS, nuevaRuta.toString());
        Files.writeString(propertiesPath, GsonUtil.getGson().toJson(obj));
    }

    public String getRutaDatos() {
        return KEY_RUTA_DATOS;
    }
}