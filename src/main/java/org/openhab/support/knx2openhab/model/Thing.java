package org.openhab.support.knx2openhab.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.knx.KnxFunctionExt;

public class Thing {

	private final ThingDescriptor descriptor;

	private final Map<String, Item> items = new HashMap<>();
	private KnxFunctionExt function;

	public Thing(ThingDescriptor thingDescriptor, KnxFunctionExt function) {
		
		this.descriptor = Objects.requireNonNull(thingDescriptor, "descriptor");
		this.function = Objects.requireNonNull(function);
		
	}

	public ThingDescriptor getDescriptor() {
		return descriptor;
	}

	public String getKey() {
		return function.getNumber().replace(' ', '_').replace('\\', '_').replace('/', '_');
	}

	public String getDescription() {
		return function.getName();
	}
	
	public String getLocation() {
		String name = function.getSpace().getName();
		return name != null ? name : "";
	}

	public Map<String, Item> getItems() {
		return Collections.unmodifiableMap(items);
	}
	
	public Map<String, Object> getContext()
	{
		return ModelUtil.getContextFromComment(function.getComment());
	}

	public Thing addItem(Item item) {
		this.items.put(item.getKey(), item);
		return this;
	}

	public void setItems(Collection<Item> items) {
		this.items.clear();
		this.items.putAll(items.stream().collect(Collectors.toMap(i -> i.getKey(), i -> i)));
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getDescriptor().getName()).append(": ").append(getKey()).append(" {");
		builder.append(getItems().values().stream().map(Object::toString).collect(Collectors.joining("; ")));
		builder.append("}");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(function.getNumber());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Thing)) {
			return false;
		}
		Thing other = (Thing) obj;
		return Objects.equals(items, other.items) && Objects.equals(descriptor, other.descriptor)
				&& Objects.equals(function.getNumber(), other.function.getNumber());
	}

}
