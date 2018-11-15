package autoo;

import java.util.HashMap;
import java.util.Map;

public final class MetaFactory {
	private static Map<Class<?>, Factory<?>> _factories = new HashMap<Class<?>, Factory<?>>();

	/**
	 * Returns a Factory that can create AutoObject's of the given class.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Factory<T> getFactory(Class<T> targetClass) {
		synchronized (_factories) {
			Factory<T> factory = (Factory<T>) _factories.get(targetClass);
			if (factory == null) {
				factory = new Factory<T>(targetClass);
				_factories.put(targetClass, factory);
			}
			return factory;
		}
	}

	private MetaFactory() {
	}

	public static <T> T create(Class<T> clss, Object... args) {
		return getFactory(clss).create(args);
	}
}
