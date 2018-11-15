package autoo.util;

public interface IListener<T,E> {
    public void onEvent(T source, E event);
}
