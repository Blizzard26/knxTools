package org.openhab.support.knx2openhab.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KNXThingDescriptor {

	private final String key;
	private final String name;
	private final List<KNXItemDescriptor> items;
	private String[] functionTypes;

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public KNXThingDescriptor(@JsonProperty("key") String key, @JsonProperty("functionTypes") String[] functionTypes,
			@JsonProperty("name") String name, @JsonProperty("items") KNXItemDescriptor... items) {
		this.key = Objects.requireNonNull(key);
		this.functionTypes = Objects.requireNonNull(functionTypes);
		this.name = Objects.requireNonNull(name);
		this.items = Arrays.asList(Objects.requireNonNull(items));
	}

	@JsonProperty("key") 
	public String getKey() {
		return key;
	}

	@JsonProperty("functionTypes")
	public String[] getFunctionTypes() {
		return functionTypes;
	}
	
	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("items")
	public List<KNXItemDescriptor> getItems() {
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
		if (!(obj instanceof KNXThingDescriptor)) {
			return false;
		}
		KNXThingDescriptor other = (KNXThingDescriptor) obj;
		return Objects.equals(key, other.key);
	}


}
