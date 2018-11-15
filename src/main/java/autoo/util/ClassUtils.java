package autoo.util;

import java.io.InputStream;

public class ClassUtils {
    private ClassUtils() {
    }
    
    public static InputStream getPackageResource(Class<?> cls, String localName) {
        String className = cls.getName();
        int dot = className.lastIndexOf('.');
        String resourceName;
        if (dot >= 0) {
            resourceName = className.substring(0, dot).replace('.', '/') + "/" + localName;
        } else {
            resourceName = localName;
        }
        return cls.getClassLoader().getResourceAsStream(resourceName);
    }
    
    /**
     * Infer a name from an object's class and the given suffix.  The name will be
     * the class name stripped of any package or outer class names, and trimmed
     * by the suffix.
     */
	public static String getShortName(Object obj, String suffix) {
        String name = obj.getClass().getName();
        int c = name.lastIndexOf(suffix);
        if (c > 0) {
            int s = Math.max(name.lastIndexOf('.'), name.lastIndexOf('$')) + 1;
            name = name.substring(s, c);
            return name.substring(0, 1).toLowerCase() + name.substring(1);
        } else {
            throw new RuntimeException("cannot find short name of " + name 
            		+ " ending with \"" + suffix + "\"");
        }
	}
}
