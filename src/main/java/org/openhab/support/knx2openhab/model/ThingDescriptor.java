package org.openhab.support.knx2openhab.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ThingDescriptor {

	private final String key;
	private final String name;
	private final List<ItemDescriptor> items;
	private String[] functionTypes;

	public ThingDescriptor(String key, String[] functionTypes, String name, ItemDescriptor... items) {
		this.key = Objects.requireNonNull(key);
		this.functionTypes = Objects.requireNonNull(functionTypes);
		this.name = Objects.requireNonNull(name);
		this.items = Arrays.asList(Objects.requireNonNull(items));
	}

	public String getKey() {
		return key;
	}
	
	public String getName() {
		return name;
	}

	public List<ItemDescriptor> getItems() {
		return Collections.unmodifiableList(items);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ThingDescriptor)) {
			return false;
		}
		ThingDescriptor other = (ThingDescriptor) obj;
		return Objects.equals(key, other.key);
	}

	public String[] getFunctionTypes() {
		return functionTypes;
	}


	
	

}
