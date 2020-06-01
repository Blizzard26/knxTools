package org.openhab.support.knx2openhab.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KNXThingDescriptor
{

    private final String key;
    private final String name;
    private final List<KNXItemDescriptor> items;
    private final String[] functionTypes;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public KNXThingDescriptor(@JsonProperty("key") final String key,
            @JsonProperty("functionTypes") final String[] functionTypes, @JsonProperty("name") final String name,
            @JsonProperty("items") final KNXItemDescriptor... items)
    {
        this.key = Objects.requireNonNull(key);
        this.functionTypes = Objects.requireNonNull(functionTypes);
        this.name = Objects.requireNonNull(name);
        this.items = Arrays.asList(Objects.requireNonNull(items));
    }

    @JsonProperty("key")
    public String getKey()
    {
        return this.key;
    }

    @JsonProperty("functionTypes")
    public String[] getFunctionTypes()
    {
        return this.functionTypes;
    }

    @JsonProperty("name")
    public String getName()
    {
        return this.name;
    }

    @JsonProperty("items")
    public List<KNXItemDescriptor> getItems()
    {
        return Collections.unmodifiableList(this.items);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.key);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof KNXThingDescriptor))
        {
            return false;
        }
        KNXThingDescriptor other = (KNXThingDescriptor) obj;
        return Objects.equals(this.key, other.key);
    }

}
