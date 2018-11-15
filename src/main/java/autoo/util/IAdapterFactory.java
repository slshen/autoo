package autoo.util;

public interface IAdapterFactory {
    
    public <T> T getAdapter(Object from, Class<T> toClass);
    
}
