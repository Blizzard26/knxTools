package org.openhab.support.knx2openhab.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.knx.KnxFunctionExt;

public class Thing {

	private final ThingDescriptor descriptor;

	private final List<Item> items = new ArrayList<>();
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
//		List<String> list = items.stream().map(Item::getDescription).distinct().filter(Objects::nonNull)
//				.collect(Collectors.toList());
//
//		if (list.size() == 1)
//			return list.get(0);
//		return getKey();
	}
	
	public String getLocation() {
		return function.getSpace().getName();
	}

	public List<Item> getItems() {
		return Collections.unmodifiableList(items);
	}

	public Thing addItem(Item action) {
		this.items.add(action);
		return this;
	}

	public void setItems(List<Item> items) {
		this.items.clear();
		this.items.addAll(items);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getDescriptor().getName()).append(": ").append(getKey()).append(" {");
		builder.append(getItems().stream().map(Object::toString).collect(Collectors.joining("; ")));
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
