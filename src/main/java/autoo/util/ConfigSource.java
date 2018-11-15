package autoo.util;

public interface ConfigSource {
    public String get(String key);
    public boolean hasKey(String key);
}
