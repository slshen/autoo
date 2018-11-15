package autoo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface so that an automatically generated object can be created at runtime.
 * The automatic object will have attributes inferred from any getters and setters.  Appropriate
 * hashCode(), toString(), and equals() methods will be gernated.  Other methods should be
 * implemented in the implementation class.
 * 
 * An automatic object is created through Factory.create().  The correct factory can
 * be retrieved through MetaFactory.getFactory().
 * 
 * The inference rules are as follows:
 * <ul>
 *   <li>A getter method and a setter method imply a mutable attribute.  The attribute
 *       will not appear in the constructor, and its value won't be used by hashCode()
 *       or equals().</li>
 *   <li>A getter method only implies a read-only attribute.  The attribute must be
 *       supplied to the constructor (Factory.create()), and its value will be used
 *       by hashCode() and equals().</li>
 *   <li>A method can be annotated with @Operation to mark methods that begin with
 *       "get" or "set" as operations and not attributes.</li>
 *   <li>All other methods are assumed to be implemented by the implementation class.</li>
 * </ul>
 * 
 * An implementation class must extend its super automatic object's implementation.  So
 * given:
 * <pre>
 *    IBase <-- extends -- IDerived
 * </pre>
 * Then:
 * <pre>
 *    IBase <-- implements -- BaseImpl
 *                                ^-- extends --\
 *    IDerived <-- implements ----------- DerivedImpl
 * </pre>
 * 
 * The automatically generated class will have an inheritance hierarchy as:
 * <pre>
 *   IBase <-- implements -- BaseImpl <-- extends -- Instance$1
 *
 *   IBase <-- implements -- BaseImpl
 *                            ^-- extends --\
 *   IDerived <-- implements ----------- DerivedImpl <-- extends -- Instance$2
 * </pre>
 * Where <code>Instance$1</code> is the generated class for <code>IBase</code>.
 * 
 * @author sls
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoObject {
    /**
     * The comma-delimited order of attributes.  
     */
    String constructor() default "";
    /**
     * The implementation class.  It is expected that this class will implement all
     * operations for this type.
     */
    Class<?> implementation() default Object.class;
    /**
     * Marks the object as abstract.  The implementation for an abstract object does not
     * have to implement all operations.
     */
    boolean isAbstract() default false;
}
