package autoo.util;

public class SystemPropertiesConfigSource implements ConfigSource {

    public String get(String key) {
        return System.getProperty(key);
    }

    public boolean hasKey(String key) {
        return System.getProperty(key) != null;
    }

}
