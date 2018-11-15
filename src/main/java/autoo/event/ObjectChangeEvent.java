package autoo.event;

import autoo.AutoObject;
import autoo.Factory;
import autoo.MetaFactory;

@AutoObject(constructor = "AttributeName,NewValue")
public interface ObjectChangeEvent {
	public static final ObjectChangeEvent[] EMPTY_ARRAY = new ObjectChangeEvent[0];
	public static final Factory<ObjectChangeEvent> factory = MetaFactory.getFactory(ObjectChangeEvent.class);

	public String getAttributeName();

	public Object getNewValue();
}
