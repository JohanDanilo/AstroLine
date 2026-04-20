package cr.ac.una.astroline.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataNotifier {

    public interface Listener {

        void onDataChanged(String fileName);
    }

    private static final List<Listener> listeners = new CopyOnWriteArrayList<>();

    private DataNotifier() {
    }

    public static void subscribe(Listener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void unsubscribe(Listener listener) {
        listeners.remove(listener);
    }

    public static void notifyChange(String fileName) {
        for (Listener l : listeners) {
            l.onDataChanged(fileName);
        }
    }
}
