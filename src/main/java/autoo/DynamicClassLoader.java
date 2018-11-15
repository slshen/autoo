package autoo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import autoo.util.Logging;

public class DynamicClassLoader extends ClassLoader {
    private static final Logger logger = Logging.getLogger(DynamicClassLoader.class, "bytecode");
    
    private static Map<ClassLoader,DynamicClassLoader> classLoaders =
        new WeakHashMap<ClassLoader,DynamicClassLoader>();
    private static AtomicInteger id = new AtomicInteger(0);
    
    synchronized static DynamicClassLoader getDynamicClassLoader(ClassLoader parent) {
        DynamicClassLoader dcl = classLoaders.get(parent);
        if (dcl == null) {
            dcl = new DynamicClassLoader(parent);
            classLoaders.put(parent, dcl);
        }
        return dcl;
    }
    
    static int getNextId() {
        return id.addAndGet(1);
    }
    
    DynamicClassLoader(ClassLoader parent) {
        super(parent);
    }

    Class<?> defineDynamicClass(String name, byte[] b) {
        if (logger.isLoggable(Level.FINEST)) {
            try {
                StringWriter sw = new StringWriter();
                ClassReader cr = new ClassReader(b);
                TraceClassVisitor tcv = new TraceClassVisitor(new PrintWriter(sw));
                cr.accept(tcv, 0);
                logger.finest("about to define dynamic class " + name + " with:\n" + sw);
            } catch (Exception e) {
                logger.log(Level.FINEST, "failed to disassemble class " + name
                        + ", but will still try to define it", e);
            }
        }
        return defineClass(name, b, 0, b.length);
    }

}
