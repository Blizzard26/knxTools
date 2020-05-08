package org.openhab.support.knx2openhab.model;

import java.util.Arrays;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KNXItemDescriptor {

	private final String key;
	private final String[] keyWords;

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public KNXItemDescriptor(@JsonProperty("key") String key, @JsonProperty("keywords") String... keywords) {
		this.key = Objects.requireNonNull(key);
		
		this.keyWords = Objects.requireNonNull(keywords, "keyWords");
		
		if (keywords.length == 0)
			throw new IllegalArgumentException();
	}
	
	@JsonProperty("key")
	public String getKey() {
		return key;
	}

	@JsonProperty("keywords")
	public String[] getKeywords() {
		return keyWords;
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
		if (!(obj instanceof KNXItemDescriptor)) {
			return false;
		}
		KNXItemDescriptor other = (KNXItemDescriptor) obj;
		return Arrays.equals(keyWords, other.keyWords);
	}
	
	

}
