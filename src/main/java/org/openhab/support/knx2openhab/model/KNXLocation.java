package org.openhab.support.knx2openhab.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class KNXLocation {

	private final String id;
	private final String name;
	private final KNXLocation parent;
	private final Set<KNXLocation> subLocations = new HashSet<>();

	public KNXLocation(String id, String name, KNXLocation parent) {
		this.id = id;
		this.name = name;
		this.parent = parent;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof KNXLocation)) {
			return false;
		}
		KNXLocation other = (KNXLocation) obj;
		return Objects.equals(id, other.id);
	}
	
	public String toString()
	{
		return name;
	}

	public KNXLocation getParent() {
		return parent;
	}

	public void addAll(List<KNXLocation> subLocations) {
		this.subLocations.addAll(subLocations);
		
	}

	public Collection<KNXLocation> getSubLocations() {
		return Collections.unmodifiableCollection(subLocations);
	}

}
