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

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    private GsonUtil() {}

    // ── Acceso a la instancia Gson ──────────────────────────────────────────

    /** Expone el Gson compartido (necesario en PathManager y donde se requiera). */
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

    // ── Operaciones de persistencia ─────────────────────────────────────────

    /** Serializa cualquier objeto (incluidas listas) a JSON en el archivo indicado. */
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

    /** Alias semántico de guardar() para cuando el objeto es explícitamente una lista. */
    public static <T> void guardarLista(List<T> lista, String nombreArchivo) {
        guardar(lista, nombreArchivo);
    }

    /** Lee un JSON y lo deserializa en un objeto del tipo indicado. */
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

    /** Lee un JSON y lo deserializa como lista del tipo indicado. */
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

    /** Comprueba si el archivo de datos existe. */
    public static boolean existe(String nombreArchivo) {
        return Files.exists(resolveFile(nombreArchivo));
    }

    /** Serializa un objeto a String JSON (sin escribir a disco). */
    public static String toJson(Object objeto) {
        return GSON.toJson(objeto);
    }
}