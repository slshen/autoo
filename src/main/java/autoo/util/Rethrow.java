package autoo.util;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to wrap a caught exception in a (subclass of) RuntimeException.
 */
public class Rethrow<T extends RuntimeException> {
    private static final Logger _logger = Logging.getLogger(Rethrow.class);
    public static final Rethrow<RuntimeException> Runtime = 
        new Rethrow<RuntimeException>(RuntimeException.class);
    private Class<T> _class;
    private Constructor<T> _constructor;
    public Rethrow(Class<T> c) {
        _class = c;
        try {
            _constructor = c.getConstructor(new Class[] { String.class, Throwable.class });
        } catch (Exception e) {
            _logger.log(Level.WARNING, "could not get exception wrapper class constructor", e);
        }
    }
    public T wrap(String msg, Throwable e) {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else if (e instanceof Error) {
            throw (Error) e;
        } else {
            if (_constructor != null) {
                try {
                    return _constructor.newInstance(new Object[] { msg, e });
                } catch (Exception e1) {
                    _logger.log(Level.WARNING, "could not create instance of " + _class 
                            + ", using RuntimeException instead", e1);
                }
            }
            throw new RuntimeException(msg, e);
        }
    }
}
