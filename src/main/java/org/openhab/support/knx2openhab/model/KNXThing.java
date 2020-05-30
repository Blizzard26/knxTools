package org.openhab.support.knx2openhab.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.knx.KnxFunctionExt;

public class KNXThing {

	private final KNXThingDescriptor descriptor;

	private final Map<String, KNXItem> items = new HashMap<>();
	private KnxFunctionExt function;

	private KNXLocation location;

	public KNXThing(KNXThingDescriptor thingDescriptor, KnxFunctionExt function, KNXLocation location) {
		this.descriptor = Objects.requireNonNull(thingDescriptor, "descriptor");
		this.function = Objects.requireNonNull(function);
		this.location = Objects.requireNonNull(location);		
	}

	public KNXThingDescriptor getDescriptor() {
		return descriptor;
	}

	public String getKey() {
		return function.getNumber().replace(' ', '_').replace('\\', '_').replace('/', '_');
	}

	public String getDescription() {
		return function.getName();
	}
	
	public String getLocation() {
		String name = location.getName();
		return name != null ? name : "";
	}

	public Map<String, KNXItem> getItems() {
		return Collections.unmodifiableMap(items);
	}
	
	public Map<String, String> getContext()
	{
		return ModelUtil.getContextFromComment(function.getComment());
	}

	public KNXThing addItem(KNXItem item) {
		this.items.put(item.getKey(), item);
		return this;
	}

	public void setItems(Collection<KNXItem> items) {
		this.items.clear();
		this.items.putAll(items.stream().collect(Collectors.toMap(i -> i.getKey(), i -> i)));
	}

	@Override
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
		if (!(obj instanceof KNXThing)) {
			return false;
		}
		KNXThing other = (KNXThing) obj;
		return Objects.equals(items, other.items) && Objects.equals(descriptor, other.descriptor)
				&& Objects.equals(function.getNumber(), other.function.getNumber());
	}

}
