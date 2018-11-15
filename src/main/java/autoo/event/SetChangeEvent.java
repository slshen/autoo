package autoo.event;

import java.util.Set;

import autoo.AutoObject;
import autoo.MetaFactory;

@AutoObject(implementation = SetChangeEvent.Impl.class)
public interface SetChangeEvent<T> {
	public Set<T> getAdded();

	public Set<T> getRemoved();

	public SetChangeEvent<T> copyAndClear();

	public abstract class Impl<IT> implements SetChangeEvent<IT>, Cloneable {
		@SuppressWarnings("unchecked")
		public SetChangeEvent<IT> copyAndClear() {
			SetChangeEvent<IT> event = (SetChangeEvent<IT>) MetaFactory.create(SetChangeEvent.class);
			event.getAdded().addAll(getAdded());
			event.getRemoved().addAll(getRemoved());
			getAdded().clear();
			getRemoved().clear();
			return event;
		}
	}

}
