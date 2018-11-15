package autoo.util;

import java.util.StringTokenizer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Logging {
    private static boolean _autoConfiguredLogging;
    public static Logger getLogger(Class<?> cls) {
        return getLogger(cls, null);
    }
    public static Logger getLogger(Class<?> cls, String suffix) {
        if (!_autoConfiguredLogging) {
            configureLogging();
            _autoConfiguredLogging = true;
        }
        String name = cls.getName();
        if (name.indexOf('.') > 0) {
            name = name.substring(0, name.lastIndexOf('.'));
        } else {
            name = null;
        }
        if (suffix != null) {
            if (name != null) {
                name = name + "." + suffix;
            } else {
                name = suffix;
            }
        }
        if (name != null) {
            return Logger.getLogger(name);
        } else {
            return Logger.getAnonymousLogger();
        }
    }
    private Logging() {
    }
    public static void configureLogging() {
        configureLoggingLevel("logging.fine", Level.FINE);
        configureLoggingLevel("logging.finer", Level.FINER);
        configureLoggingLevel("logging.finest", Level.FINEST);
    }
        
    private static void configureLoggingLevel(String name, Level level) {
        Config config = Config.getInstance();
        String on = config.get(name);
        if (on != null && on.length() > 0 && !on.equalsIgnoreCase("false")) {
            Logger.getLogger("").setLevel(level);
            Handler[] handlers = Logger.getLogger("").getHandlers();
            for (int i = 0; i < handlers.length; i++) {
                handlers[i].setLevel(level);
            }
            if (on.equalsIgnoreCase("true")) {
                Logger.getLogger("").setLevel(level);
            } else {
                StringTokenizer tokens = new StringTokenizer(on, ",");
                while (tokens.hasMoreTokens()) {
                    String loggerName = tokens.nextToken();
                    Logger.getLogger(loggerName).setLevel(level);
                }
            }
        }
    }

}
