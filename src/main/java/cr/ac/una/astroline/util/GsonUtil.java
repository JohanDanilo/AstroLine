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

/**
 * Utilidad centralizada para leer y escribir archivos JSON con Gson.
 * Todos los módulos deben usar esta clase para persistencia de datos.
 * Los archivos JSON se guardan en la carpeta data/ del directorio raíz.
 *
 * @author AstroLine
 */
public class GsonUtil {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    private static final String DATA_DIR = "data/";

    private GsonUtil() {
    }

    /**
     * Guarda un objeto como archivo JSON en la carpeta data/.
     * Si la carpeta no existe la crea automáticamente.
     *
     * Ejemplo: GsonUtil.guardar(empresa, "empresa.json");
     *
     * @param objeto       el objeto a serializar
     * @param nombreArchivo nombre del archivo JSON destino
     */
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

    /**
     * Lee un objeto desde un archivo JSON.
     * Retorna null si el archivo no existe.
     *
     * Ejemplo: Empresa e = GsonUtil.leer("empresa.json", Empresa.class);
     *
     * @param nombreArchivo nombre del archivo JSON a leer
     * @param clase         clase del objeto a deserializar
     * @return el objeto deserializado o null si no existe el archivo
     */
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

    /**
     * Lee una lista de objetos desde un archivo JSON.
     * Retorna una lista vacía si el archivo no existe.
     *
     * Ejemplo: List<Tramite> lista = GsonUtil.leerLista("tramites.json", Tramite.class);
     *
     * @param nombreArchivo nombre del archivo JSON a leer
     * @param clase         clase de los objetos dentro de la lista
     * @return la lista deserializada o lista vacía si no existe el archivo
     */
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

    /**
     * Verifica si un archivo de datos existe en la carpeta data/.
     *
     * @param nombreArchivo nombre del archivo a verificar
     * @return true si el archivo existe
     */
    public static boolean existe(String nombreArchivo) {
        return Files.exists(Paths.get(DATA_DIR, nombreArchivo));
    }
    
    public static String getDataDir(){
        return DATA_DIR;
    }
    
    public static void guardarYPropagar(Object objeto, String nombreArchivo) {
        guardar(objeto, nombreArchivo);
        SyncManager.getInstancia().propagar(nombreArchivo);
    }
    
    public static String toJson(Object objeto) {
        return gson.toJson(objeto);
    }
}