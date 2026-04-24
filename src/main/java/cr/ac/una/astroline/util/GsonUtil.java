package cr.ac.una.astroline.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class GsonUtil {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    private GsonUtil() {}

    public static Gson getGson() {
        return GSON;
    }
    
    public static Path getDataDir() {
        return PathManager.getDataPath();
    }

    /** Resuelve un archivo dentro del directorio de datos. */
    private static Path resolveFile(String nombreArchivo) {
        return getDataDir().resolve(nombreArchivo);
    }

    /** Garantiza que el directorio de datos exista antes de escribir. */
    private static void ensureDataDir() throws IOException {
        Files.createDirectories(getDataDir());
    }

    public static void guardar(Object objeto, String nombreArchivo) {
        try {
            ensureDataDir();
            Path filePath = resolveFile(nombreArchivo);
            try (Writer writer = new OutputStreamWriter(
                    new FileOutputStream(filePath.toFile()), StandardCharsets.UTF_8)) {
                GSON.toJson(objeto, writer);
            }
        } catch (IOException e) {
            System.err.println("[GsonUtil] Error al guardar " + nombreArchivo + ": " + e.getMessage());
        }
    }

    public static <T> void guardarLista(List<T> lista, String nombreArchivo) {
        guardar(lista, nombreArchivo);
    }

    public static <T> T leer(String nombreArchivo, Class<T> clase) {
        Path filePath = resolveFile(nombreArchivo);
        if (!Files.exists(filePath)) return null;
        try (Reader reader = new InputStreamReader(
                new FileInputStream(filePath.toFile()), StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, clase);
        } catch (IOException e) {
            System.err.println("[GsonUtil] Error al leer " + nombreArchivo + ": " + e.getMessage());
            return null;
        }
    }

    public static <T> List<T> leerLista(String nombreArchivo, Class<T> clase) {
        Path filePath = resolveFile(nombreArchivo);
        if (!Files.exists(filePath)) return new ArrayList<>();
        try (Reader reader = new InputStreamReader(
                new FileInputStream(filePath.toFile()), StandardCharsets.UTF_8)) {
            Type listType = TypeToken.getParameterized(List.class, clase).getType();
            List<T> resultado = GSON.fromJson(reader, listType);
            return resultado != null ? resultado : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("[GsonUtil] Error al leer lista " + nombreArchivo + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static boolean existe(String nombreArchivo) {
        return Files.exists(resolveFile(nombreArchivo));
    }

    public static String toJson(Object objeto) {
        return GSON.toJson(objeto);
    }
}
