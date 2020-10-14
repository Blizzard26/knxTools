package org.openhab.support.knx2openhab.model;

import java.util.Map;
import java.util.Objects;

import org.knx.xml.KnxGroupAddressT;

public class KNXItem
{

    private final KNXItemDescriptor itemDescriptor;
    private final KnxGroupAddressT groupAddress;
    private final boolean readable;
    private final boolean writeable;
    private Map<String, String> context;

    public KNXItem(final KNXItemDescriptor itemDescriptor, final KnxGroupAddressT groupAddress, final boolean readable,
            final boolean writeable)
    {
        this.itemDescriptor = itemDescriptor;
        this.groupAddress = groupAddress;
        this.readable = readable;
        this.writeable = writeable;
    }

    public String getKey()
    {
        return itemDescriptor.getKey();
    }

    public KNXItemDescriptor getItemDescriptor()
    {
        return itemDescriptor;
    }

    public String getAddress()
    {
        return ModelUtil.getAddressAsString(groupAddress);
    }

    public String getDescription()
    {
        return groupAddress.getDescription();
    }

    public String getName()
    {
        if (itemDescriptor.getLabel() != null)
            return itemDescriptor.getLabel();
        if (getContext().containsKey("label"))
            return context.get("label");
        return groupAddress.getName();
    }

    public Map<String, String> getContext()
    {
        if (context == null)
        {
            context = ModelUtil.getContextFromComment(groupAddress.getComment());
        }
        return context;
    }

    public String getType()
    {
        if (itemDescriptor.getOverrideType() != null)
            return itemDescriptor.getOverrideType();
        return ModelUtil.getDataPointTypeAsString(groupAddress);
    }

    public boolean isReadable()
    {
        return readable;
    }

    public boolean isWriteable()
    {
        return writeable;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(getItemDescriptor().getKey()).append(" => ").append(getAddress());
        if (getDescription() != null)
        {
            builder.append(" (").append(getDescription()).append(")");
        }
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(groupAddress.getAddress());
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof KNXItem))
            return false;
        KNXItem other = (KNXItem) obj;
        return Objects.equals(getAddress(), other.getAddress());
    }

}
