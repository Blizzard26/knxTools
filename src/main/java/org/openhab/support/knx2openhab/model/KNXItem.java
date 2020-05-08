package org.openhab.support.knx2openhab.model;

import java.util.Map;
import java.util.Objects;

import org.knx.KnxGroupAddressExt;

public class KNXItem {

	private KNXItemDescriptor itemDescriptor;
	private KnxGroupAddressExt groupAddress;
	
	public KNXItem(KNXItemDescriptor itemDescriptor, KnxGroupAddressExt groupAddress) {
		this.itemDescriptor = itemDescriptor;
		this.groupAddress = groupAddress;
	}

	public String getKey()
	{
		return itemDescriptor.getKey();
	}

	public KNXItemDescriptor getItemDescriptor() {
		return itemDescriptor;
	}

	public String getAddress() {
		return groupAddress.getAddressAsString();
	}

	public String getDescription() {
		return groupAddress.getDescription();
	}
	
	public Map<String, String> getContext()
	{
		return ModelUtil.getContextFromComment(groupAddress.getComment());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getItemDescriptor().getKey()).append(" => ").append(getAddress());
		if (getDescription() != null)
			builder.append(" (").append(getDescription()).append(")");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(groupAddress.getAddress());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof KNXItem)) {
			return false;
		}
		KNXItem other = (KNXItem) obj;
		return Objects.equals(getAddress(), other.getAddress());
	}

	public String getType() {
		return groupAddress.getDataPointTypeAsString();
	}

	
	
}
