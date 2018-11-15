package autoo.event;

import java.util.ArrayList;
import java.util.List;

import autoo.util.IListener;
import autoo.util.ListenerList;

public class Publisher<T> {
    
    private ListenerList<T,ObjectChangeEvent[]> listeners = 
        new ListenerList<T,ObjectChangeEvent[]>();
    private List<ObjectChangeEvent> events = new ArrayList<ObjectChangeEvent>();
    
    public void attributeChanged(String name, Object newValue) {
        events.add(ObjectChangeEvent.factory.create(name, newValue));
    }
    
    public void publishByte(String name, byte b) {
        events.add(ObjectChangeEvent.factory.create(name, b));
    }

    public void publishShort(String name, short s) {
        events.add(ObjectChangeEvent.factory.create(name, s));
    }

    public void publishChar(String name, char c) {
        events.add(ObjectChangeEvent.factory.create(name, c));
    }

    public void publishInt(String name, int i) {
        events.add(ObjectChangeEvent.factory.create(name, i));
    }

    public void publishLong(String name, long l) {
        events.add(ObjectChangeEvent.factory.create(name, l));
    }

    public void publishFloat(String name, float f) {
        events.add(ObjectChangeEvent.factory.create(name, f));
    }

    public void publishDouble(String name, double d) {
        events.add(ObjectChangeEvent.factory.create(name, d));
    }

    public void publishBoolean(String name, byte b) {
        events.add(ObjectChangeEvent.factory.create(name, b));
    }

    public void publish(String name, Object o) {
        events.add(ObjectChangeEvent.factory.create(name, o));
    }
    
    public void publishChanges(T source) {
        listeners.notify(source, events.toArray(ObjectChangeEvent.EMPTY_ARRAY));
        events.clear();
    }

    public void addListener(IListener<T,ObjectChangeEvent[]> listener) {
        listeners.addListener(listener);
    }

    public void removeListener(IListener<T,ObjectChangeEvent[]> listener) {
        listeners.removeListener(listener);
    }

}
