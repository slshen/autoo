package autoo.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public final class Config {
    private static final Config _instance = new Config();
    private List<ConfigSource> _sources = new ArrayList<ConfigSource>();
    public static Config getInstance() {
        return _instance;
    }
    private Config() {
        addSource(new SystemPropertiesConfigSource());
    }

    public String get(String key) {
        for (Iterator<ConfigSource> iter = _sources.iterator(); iter.hasNext();) {
            ConfigSource source = (ConfigSource) iter.next();
            if (source.hasKey(key)) {
                return source.get(key);
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getList(String key) {
        String value = get(key);
        if (value != null) {
            StringTokenizer tokens = new StringTokenizer(value, ",");
            List<String> result = new LinkedList<String>();
            while (tokens.hasMoreTokens()) {
                result.add(tokens.nextToken());
            }
            return result;
        }
        return Collections.EMPTY_LIST;
    }
    public String get(String key, String dflt) {
        String val = get(key);
        if (val != null) {
            return val;
        } else {
            return dflt;
        }
    }
    public void addSource(ConfigSource source) {
        _sources.add(source);
    }
    public Integer getInteger(String key) {
        String val = get(key);
        if (val != null) {
            return Integer.valueOf(val);
        }
        return null;
    }
    public boolean getBoolean(String key) {
        String val = get(key);
        return val != null && val.equalsIgnoreCase("true");
    }
	public File getDirectory(String name, boolean autoCreate) {
		String val = get(name);
		if (val != null) {
			File dir = new File(val);
			if (autoCreate && !dir.isDirectory()) {
				if (!dir.mkdirs()) {
					throw new RuntimeException("could not create create directory " + dir);
				}
			}
			return dir;
		}
		return null;
	}
	public Double getDouble(String name) {
		String val = get(name);
		if (val != null) {
			return new Double(val);
		}
		return null;
	}
}
