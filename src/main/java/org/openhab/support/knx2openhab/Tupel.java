package org.openhab.support.knx2openhab;

public class Tupel<K, V>
{

    private final K first;
    private final V second;

    public Tupel(final K first, final V second)
    {
        this.first = first;
        this.second = second;
    }

    public K getFirst()
    {
        return this.first;
    }

    public V getSecond()
    {
        return this.second;
    }

    public K first()
    {
        return this.first;
    }

    public V second()
    {
        return this.second;
    }

}
