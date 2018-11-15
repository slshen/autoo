package autoo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import autoo.event.IPublishable;

public final class Factory<T> {
    private Class<T> _targetClass;
    private Collection<AttributeType> _constructorAttributes;
    private Collection<AttributeType> _attributes;
    private Factory<?> _parentFactory;
    private boolean _isAbstract;
    private boolean _hasAutoId;
    private boolean _isPublishable;
    private Class<?> _implementationClass;
    private Class<T> _instanceClass;
    private Constructor<T> _instanceConstructor;
    
    Factory(Class<T> targetClass) {
        _targetClass = targetClass;
        if (!_targetClass.isInterface()) {
            throw new AoException("can only create factories from interfaces");
        }
        AutoObject note = _targetClass.getAnnotation(AutoObject.class);
        if (note == null) {
            throw new AoException("can only create factories from interfaces with the NcgObject" 
                    + " annotation");
        }
        _isAbstract = note.isAbstract();
        Map<String, AttributeType> attributeMap = new HashMap<String,AttributeType>();
        // find getters first -- these define what attributes the object has
        for (Method method : targetClass.getDeclaredMethods()) {
            if (isGetter(method)) {
                AttributeType type = new AttributeType(this, method);
                attributeMap.put(type.getName(), type);
                if (type.isAutoId()) {
                    _hasAutoId = true;
                    if (!type.getDeclaredType().equals(Long.TYPE)) {
                        throw new AoException("auto id attributes must be of type long");
                    }
                }
            }
        }
        // process setters
        for (Method method : targetClass.getDeclaredMethods()) {
            if (isSetter(method)) {
                AttributeType type =
                    attributeMap.get(method.getName().substring(3));
                if (type == null) {
                    throw new AoException("setter " + method.getName() 
                            + " must have a corresponding getter");
                }
                if (type.isCollection()) {
                    throw new AoException("collection or map attribute " + type.getName()
                            + " must not have a setter");
                }
                type.setWritable(true);
            }
        }
        // at this point we have all attributes declared in this type, now just
        // need to put them in the right order
        _constructorAttributes = new ArrayList<AttributeType>();
        _attributes = new ArrayList<AttributeType>();
        StringTokenizer tokens = new StringTokenizer(note.constructor(), ",");
        while (tokens.hasMoreTokens()) {
            String name = tokens.nextToken();
            AttributeType type = attributeMap.get(name);
            if (type == null) {
                throw new AoException("attribute " + name + " specified in constructor "
                        + " does not actually exist in the type " + targetClass);
            }
            if (type.isCollection()) {
                throw new AoException("collection " + name + " should not be specified in constructor");
            }
            _constructorAttributes.add(type);
            _attributes.add(type);
            attributeMap.remove(name);
        }
        _attributes.addAll(attributeMap.values());
        
        // check to make sure all constructor attributes have been specified
        for (AttributeType attr : _attributes) {
            if (!attr.isWritable()
                    && !attr.isCollection()
                    && !attr.isAutoId()
                    && !_constructorAttributes.contains(attr)) { 
                throw new AoException("constructor should include " + attr);
            }
        }
        
        // find parents, determine if publishable
        for (Class<?> parent : targetClass.getInterfaces()) {
            if (parent.isAnnotationPresent(AutoObject.class)) {
                if (_parentFactory == null) {
                    _parentFactory = MetaFactory.getFactory(parent);
                } else {
                    throw new AoException(targetClass + " can only extend a single NcgObject");
                }
            }
            if (parent.equals(IPublishable.class)) {
                _isPublishable = true;
            }
        }
        if (!_isPublishable && _parentFactory != null) {
            _isPublishable = _parentFactory.isPublishable();
        }
        
        // count operations, find implementation class
        _implementationClass = note.implementation();
        if (!_isAbstract) {
            // TODO these checks are not completely correct - consider if base is abstract
            // but derived does not implement a method
            for (Method method : targetClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Operation.class)
                        || (!isGetter(method) && !isSetter(method))) {
                    if (!hasImplementation(method.getName(), method.getParameterTypes())) {
                        throw new AoException("implementation " + _implementationClass + " must implement "
                                + method);
                    }
                }
            }
            if (_implementationClass != Object.class && !_targetClass.isAssignableFrom(_implementationClass)) {
                throw new AoException("implementation " + _implementationClass + " must implement "
                        + _targetClass);
            }
        }
        if (_parentFactory != null 
                && _implementationClass.getSuperclass() != _parentFactory.getImplementationClass()) {
            throw new AoException("implementation " + _implementationClass + " must extend "
                    + _parentFactory.getImplementationClass());
        }
        // check constructor
        try {
            _implementationClass.getConstructor(new Class[0]);
        } catch (NoSuchMethodException nsme) {
            throw new AoException("implementation " + _implementationClass 
                    + " must have a no argument constructor");
        }

        Compiler<T> compiler = new Compiler<T>(this);
        _instanceClass = compiler.getInstanceClass();
        _instanceConstructor = compiler.getInstanceConstructor();
    }

    /**
     * @param method
     * @return
     */
    private boolean isSetter(Method method) {
        return method.getName().startsWith("set")
                && method.getParameterTypes().length == 1
                && !method.isAnnotationPresent(Operation.class);
    }

    /**
     * @param method
     * @return
     */
    private boolean isGetter(Method method) {
        return method.getName().startsWith("get")
                && method.getParameterTypes().length == 0
                && !method.isAnnotationPresent(Operation.class);
    }

    public T create(Object... args) {
        try {
            T instance = _instanceConstructor.newInstance(args);
            return instance;
        } catch (Exception e) {
            throw AoException.rethrow.wrap("could not create object", e);
        }
    }
    
    Factory<?> getParentFactory() {
        return _parentFactory;
    }
    Class<T> getTargetClass() {
        return _targetClass;
    }
    Collection<AttributeType> getAttributes() {
        return _attributes;
    }
    public String toString() {
        return getClass().getName() + "{targetClass=" + _targetClass + "}";
    }
    boolean hasAutoId() {
        return _hasAutoId;
    }
    boolean isPublishable() {
        return _isPublishable;
    }
    int getParentFactoryCount() {
        if (_parentFactory != null) {
            return 1 + _parentFactory.getParentFactoryCount();
        } else {
            return 0;
        }
    }
    Collection<AttributeType> getConstructorAttributes() {
        return _constructorAttributes;
    }
    Class<?> getImplementationClass() {
        return _implementationClass;
    }
    Class<T> getInstanceClass() {
        return _instanceClass;
    }
    boolean hasImplementation(String methodName, Class<?>[] parameterTypes) {
        try {
            _implementationClass.getMethod(methodName, parameterTypes);
            return true;
        } catch (NoSuchMethodException nsme) {
            return false;
        }
    }

}
