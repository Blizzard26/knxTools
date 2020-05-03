package org.openhab.support.knx2openhab.model;

import java.util.Arrays;
import java.util.Objects;

public class ItemDescriptor {

	private String[] action;

	public ItemDescriptor(String... actions) {
		this.action = Objects.requireNonNull(actions, "actions");
		
		if (actions.length == 0)
			throw new IllegalArgumentException();
	}

	public String[] getActions() {
		return action;
	}

	public String getPrimaryAction() {
		return action[0];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(action);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ItemDescriptor)) {
			return false;
		}
		ItemDescriptor other = (ItemDescriptor) obj;
		return Arrays.equals(action, other.action);
	}
	
	

}
