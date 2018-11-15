package autoo.event;

import java.util.Set;

import autoo.AutoObject;
import autoo.util.IListener;

@AutoObject(implementation = PublishableSetImpl.class)
public interface PublishableSet<T> extends Set<T> {
    public Set<T> getUnderylingSet();
    public void addListener(IListener<PublishableSet<T>, SetChangeEvent<T>> listener);
    public void removeListener(IListener<PublishableSet<T>, SetChangeEvent<T>> listener);
    public void publishChanges();
}
