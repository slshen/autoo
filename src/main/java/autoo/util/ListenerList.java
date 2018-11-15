package autoo.util;

import java.util.ArrayList;
import java.util.List;

public class ListenerList<T,E> {
    private IListener<T,E> listener;
    private List<IListener<T,E>> listeners;

    public ListenerList() {
    }
    public synchronized void notify(T source, E event) {
        if (listener != null) {
            listener.onEvent(source, event);
        }
        if (listeners != null) {
            for (IListener<T,E> listener : listeners) {
                listener.onEvent(source,event);
            }
        }
    }
    public synchronized void addListener(IListener<T,E> listener) {
        if (this.listener == null) {
            this.listener = listener;
        } else if (listeners == null) {
            listeners = new ArrayList<IListener<T,E>>(5);
            listeners.add(listener);
        } else {
            listeners.add(listener);
        }
    }
    public synchronized void removeListener(IListener<T,E> listener) {
        if (this.listener == listener) {
            this.listener = null;
        } else {
            listeners.remove(listener);
        }
    }
}
