package cr.ac.una.astroline.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author JohanDanilo
 */

public class DataNotifier {

    public interface Listener {
        void onDataChanged(String fileName);
    }

    private static final List<Listener> listeners = new CopyOnWriteArrayList<>();

    public static void subscribe(Listener listener) {
        listeners.add(listener);
    }

    public static void notifyChange(String fileName) {
        for (Listener l : listeners) {
            l.onDataChanged(fileName);
        }
    }
}