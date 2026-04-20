package cr.ac.una.astroline.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Bus de eventos para notificar cambios en archivos de datos.
 * Soporta suscripción y cancelación explícita de listeners.
 *
 * @author JohanDanilo
 */
public class DataNotifier {

    public interface Listener {
        void onDataChanged(String fileName);
    }

    private static final List<Listener> listeners = new CopyOnWriteArrayList<>();

    private DataNotifier() {}

    /** Registra un listener. Guardar la referencia para poder desuscribir después. */
    public static void subscribe(Listener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Cancela el registro de un listener.
     * Llamar desde el controlador al salir de la vista si FlowController no cachea.
     */
    public static void unsubscribe(Listener listener) {
        listeners.remove(listener);
    }

    /** Notifica a todos los listeners suscritos. Se llama desde hilos de background;
     *  los listeners deben usar Platform.runLater() para actualizar la UI. */
    public static void notifyChange(String fileName) {
        for (Listener l : listeners) {
            l.onDataChanged(fileName);
        }
    }
}