package org.openhab.support.knx2openhab.model;

import java.util.Arrays;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KNXItemDescriptor
{

    private final String key;
    private final String[] keyWords;
    private final String overrideType;
    private final String label;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public KNXItemDescriptor(@JsonProperty("key") final String key, @JsonProperty("keywords") final String[] keywords,
            @JsonProperty("label") final String label, @JsonProperty("overrideType") final String overrideType)
    {
        this.key = Objects.requireNonNull(key);

        this.keyWords = Objects.requireNonNull(keywords, "keyWords");

        if (keywords.length == 0)
            throw new IllegalArgumentException();

        this.label = label;

        this.overrideType = overrideType;
    }

    @JsonProperty("key")
    public String getKey()
    {
        return this.key;
    }

    @JsonProperty("keywords")
    public String[] getKeywords()
    {
        return this.keyWords;
    }

    @JsonProperty("label")
    public String getLabel()
    {
        return this.label;
    }

    @JsonProperty("overrideType")
    public String getOverrideType()
    {
        return this.overrideType;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.keyWords);
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof KNXItemDescriptor))
            return false;
        KNXItemDescriptor other = (KNXItemDescriptor) obj;
        return Arrays.equals(this.keyWords, other.keyWords);
    }

}
