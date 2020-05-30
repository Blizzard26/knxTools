package org.openhab.support.knx2openhab;

public class Tupel<K, V> {

	private final K first;
	private final V second;

	public Tupel(K first, V second) {
		this.first = first;
		this.second = second;
	}

	public K getFirst() {
		return first;
	}

	public V getSecond() {
		return second;
	}
	
	public K first() {
		return first;
	}
	
	public V second() {
		return second;
	}

}
