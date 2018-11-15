package autoo.event;

import autoo.util.IListener;

public interface IPublishable<T> {
    public void publishChanges();
    public void addListener(IListener<T,ObjectChangeEvent[]> listener);
    public void removeListener(IListener<T,ObjectChangeEvent[]> listener);
}
