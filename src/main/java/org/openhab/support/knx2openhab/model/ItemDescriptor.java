package org.openhab.support.knx2openhab.model;

import java.util.Arrays;
import java.util.Objects;

public class ItemDescriptor {

	private String[] keyWords;
	private String key;

	public ItemDescriptor(String key, String... keyWords) {
		this.key = Objects.requireNonNull(key);
		
		this.keyWords = Objects.requireNonNull(keyWords, "keyWords");
		
		if (keyWords.length == 0)
			throw new IllegalArgumentException();
	}

	public String[] getKeywords() {
		return keyWords;
	}

	public String getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(keyWords);
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
		return Arrays.equals(keyWords, other.keyWords);
	}
	
	

}
