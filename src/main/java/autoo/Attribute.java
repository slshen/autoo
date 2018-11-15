package autoo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Attribute {
    public final class DefaultType { private DefaultType() {} }
    boolean isAutoId() default false;
    /**
     * Set the actual type for a collection or map.  The collection will be created through
     * its no-argument constructor. Alternatively, an implementation can
     * provide a method initAttributeName() that returns an appropriate collection
     * class.
     */
    Class<?> actualType() default DefaultType.class;
}
