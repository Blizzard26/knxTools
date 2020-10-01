package org.openhab.support.knx2openhab.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.knx.xml.KnxFunctionT;
import org.knx.xml.KnxSpaceT;

public class KNXThing
{

    private final KNXThingDescriptor descriptor;

    private final Map<String, KNXItem> items = new HashMap<>();
    private final KnxFunctionT function;
    private Map<String, String> context;

    public KNXThing(final KNXThingDescriptor thingDescriptor, final KnxFunctionT function)
    {
        this.descriptor = Objects.requireNonNull(thingDescriptor, "descriptor");
        this.function = Objects.requireNonNull(function);
    }

    public KNXThingDescriptor getDescriptor()
    {
        return this.descriptor;
    }

    public String getKey()
    {
        return this.function.getNumber().replace(' ', '_').replace('\\', '_').replace('/', '_');
    }

    public String getDescription()
    {
        return this.function.getName();
    }

    public String getLocation()
    {
        String name = getSpace().getName();
        return name != null ? name : "";
    }

    public KnxSpaceT getSpace()
    {
        return (KnxSpaceT) this.function.getParent();
    }

    public Map<String, KNXItem> getItems()
    {
        return Collections.unmodifiableMap(this.items);
    }

    public Map<String, String> getContext()
    {
        if (context == null)
        {
            context = ModelUtil.getContextFromComment(this.function.getComment());
        }
        return context;
    }

    public KNXThing addItem(final KNXItem item)
    {
        this.items.put(item.getKey(), item);
        return this;
    }

    public void setItems(final Collection<KNXItem> items)
    {
        this.items.clear();
        this.items.putAll(items.stream().collect(Collectors.toMap(KNXItem::getKey, i -> i)));
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(getDescriptor().getName()).append(": ").append(getKey()).append(" {");
        builder.append(getItems().values().stream().map(Object::toString).collect(Collectors.joining("; ")));
        builder.append("}");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.function.getNumber());
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof KNXThing))
            return false;
        KNXThing other = (KNXThing) obj;
        return Objects.equals(this.items, other.items) && Objects.equals(this.descriptor, other.descriptor)
                && Objects.equals(this.function.getNumber(), other.function.getNumber());
    }

}
