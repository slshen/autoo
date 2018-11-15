package autoo.event;

import java.util.Collection;
import java.util.Iterator;

import autoo.MetaFactory;
import autoo.util.IListener;
import autoo.util.ListenerList;

public abstract class PublishableSetImpl<T> implements PublishableSet<T> {
    
    private class Iter implements Iterator<T> {
        
        private Iterator<T> iterator;
        private T current;

        Iter(Iterator<T> iterator) {
            this.iterator = iterator;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public T next() {
            return current = iterator.next();
        }

        public void remove() {
            iterator.remove();
            event.getAdded().remove(current);
            event.getRemoved().add(current);
        }
    }
    
    private ListenerList<PublishableSet<T>, SetChangeEvent<T>> listeners =
        new ListenerList<PublishableSet<T>, SetChangeEvent<T>>();
    @SuppressWarnings("unchecked")
    private SetChangeEvent<T> event = 
        MetaFactory.create(SetChangeEvent.class);
    
    public void addListener(IListener<PublishableSet<T>, SetChangeEvent<T>> listener) {
        listeners.addListener(listener);
    }

    public void removeListener(IListener<PublishableSet<T>, SetChangeEvent<T>> listener) {
        listeners.removeListener(listener);
    }
    
    public void publishChanges() {
        listeners.notify(this, event.copyAndClear());
    }
    
    private void elementRemoved(T o) {
        if (!event.getAdded().remove(o)) {
            event.getRemoved().add(o);
        }
    }
    
    private void elementAdded(T o) {
        if (!event.getRemoved().remove(o)) {
            event.getAdded().add(o);
        }
    }
    
    public boolean add(T o) {
        boolean result = getUnderylingSet().add(o);
        if (result) {
            elementAdded(o);
        }
        return result;
    }

    public boolean addAll(Collection<? extends T> c) {
        boolean result = getUnderylingSet().addAll(c);
        if (result) {
            for (Iterator<? extends T> iter = c.iterator(); iter.hasNext();) {
                T element = iter.next();
                elementAdded(element);
            }
        }
        return result;
    }

    public void clear() {
        event.getAdded().clear();
        event.getRemoved().addAll(getUnderylingSet());
        getUnderylingSet().clear();
    }

    public boolean contains(Object o) {
        return getUnderylingSet().contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return getUnderylingSet().containsAll(c);
    }

    public boolean isEmpty() {
        return getUnderylingSet().isEmpty();
    }

    public Iterator<T> iterator() {
        return new Iter(getUnderylingSet().iterator());
    }

    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        if (getUnderylingSet().remove(o)) {
            elementRemoved((T) o);
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean removeAll(Collection<?> c) {
        if (getUnderylingSet().removeAll(c)) {
            for (Iterator<?> iter = c.iterator(); iter.hasNext();) {
                T element = (T) iter.next();
                elementRemoved(element);
            }
            return true;
        }
        return false;
    }

    public boolean retainAll(Collection<?> c) {
        if (getUnderylingSet().retainAll(c)) {
            event.getAdded().retainAll(c);
            event.getRemoved().removeAll(c);
            return true;
        }
        return false;
    }

    public int size() {
        return getUnderylingSet().size();

    }

    public Object[] toArray() {
        return getUnderylingSet().toArray();
    }

    public <AT> AT[] toArray(AT[] a) {
        return getUnderylingSet().toArray(a);
    }

    @Override
    public int hashCode() {
        return getUnderylingSet().hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        return getUnderylingSet().equals(o);
    }
    
}