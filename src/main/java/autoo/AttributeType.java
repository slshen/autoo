package autoo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import autoo.util.Logging;

public class AttributeType {
	@SuppressWarnings("unused")
	private static final Logger _logger = Logging.getLogger(AttributeType.class);
	private static Map<Class<?>, Class<?>> _defaultCollectionType = new HashMap<>();

	private static void initCollectionType(Class<?> declaredType, Class<?> defaultImpl) {
		_defaultCollectionType.put(declaredType, defaultImpl);
	}

	static {
		initCollectionType(Collection.class, ArrayList.class);
		initCollectionType(List.class, ArrayList.class);
		initCollectionType(Queue.class, LinkedList.class);
		initCollectionType(Set.class, HashSet.class);
		initCollectionType(SortedSet.class, TreeSet.class);
		initCollectionType(Map.class, HashMap.class);
		initCollectionType(SortedMap.class, TreeMap.class);
	}
	private String _name;
	private boolean _isAutoId;
	private boolean _isWritable;
	private Class<?> _actualType;
	private Class<?> _declaredType;
	private Factory<?> _owner;
	private boolean _isAutoObject;

	AttributeType(Factory<?> owner, Method getter) {
		_owner = owner;
		_name = getter.getName().substring(3);
		_declaredType = getter.getReturnType();
		_actualType = _declaredType;
		Attribute note = getter.getAnnotation(Attribute.class);
		if (note != null) {
			_isAutoId = note.isAutoId();
		}
		_isAutoObject = _declaredType.isAnnotationPresent(AutoObject.class);
		if (isCollection()) {
			if (note == null || note.actualType().equals(Attribute.DefaultType.class)) {
				_actualType = _defaultCollectionType.get(_declaredType);
				if (_actualType == null && !_isAutoObject) {
					throw new AoException("collection type " + _declaredType + " has no default");
				}
			} else {
				_actualType = note.actualType();
			}
			if (!_isAutoObject && !_declaredType.isAssignableFrom(_actualType)) {
				throw new AoException(
						"actual type " + _actualType + " is incompatiable with declared type " + _declaredType);
			}
		}
	}

	String getName() {
		return _name;
	}

	boolean isAutoId() {
		return _isAutoId;
	}

	boolean isAutoObject() {
		return _isAutoObject;
	}

	boolean isWritable() {
		return _isWritable;
	}

	void setWritable(boolean isWritable) {
		_isWritable = isWritable;
	}

	Class<?> getDeclaredType() {
		return _declaredType;
	}

	Class<?> getActualType() {
		return _actualType;
	}

	boolean isCollection() {
		return Collection.class.isAssignableFrom(_declaredType) || Map.class.isAssignableFrom(_declaredType);
	}

	Factory<?> getOwner() {
		return _owner;
	}

	public String toString() {
		return _name + ":" + _declaredType.getName();
	}
}