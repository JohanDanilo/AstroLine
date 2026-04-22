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

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    private static final String DATA_DIR = "data/";

    private GsonUtil() {
    }

    public static void guardar(Object objeto, String nombreArchivo) {
        try {
            Path dirPath = Paths.get(DATA_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            Path filePath = dirPath.resolve(nombreArchivo);
            try (Writer writer = new OutputStreamWriter(
                    new FileOutputStream(filePath.toFile()), StandardCharsets.UTF_8)) {
                gson.toJson(objeto, writer);
            }
        } catch (IOException e) {
            System.err.println("Error al guardar " + nombreArchivo + ": " + e.getMessage());
        }
    }

    public static <T> T leer(String nombreArchivo, Class<T> clase) {
        Path filePath = Paths.get(DATA_DIR, nombreArchivo);
        if (!Files.exists(filePath)) {
            return null;
        }
        try (Reader reader = new InputStreamReader(
                new FileInputStream(filePath.toFile()), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, clase);
        } catch (IOException e) {
            System.err.println("Error al leer " + nombreArchivo + ": " + e.getMessage());
            return null;
        }
    }

    public static <T> List<T> leerLista(String nombreArchivo, Class<T> clase) {
        Path filePath = Paths.get(DATA_DIR, nombreArchivo);
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }
        try (Reader reader = new InputStreamReader(
                new FileInputStream(filePath.toFile()), StandardCharsets.UTF_8)) {
            Type listType = TypeToken.getParameterized(List.class, clase).getType();
            List<T> resultado = gson.fromJson(reader, listType);
            return resultado != null ? resultado : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error al leer lista " + nombreArchivo + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static boolean existe(String nombreArchivo) {
        return Files.exists(Paths.get(DATA_DIR, nombreArchivo));
    }

    public static String getDataDir() {
        return DATA_DIR;
    }

    public static String toJson(Object objeto) {
        return gson.toJson(objeto);
    }
}
