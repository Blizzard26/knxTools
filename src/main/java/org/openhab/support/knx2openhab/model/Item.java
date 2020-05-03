package org.openhab.support.knx2openhab.model;

import java.util.Objects;

import org.knx.KnxGroupAddressExt;

public class Item {

	private ItemDescriptor itemDescriptor;
	private KnxGroupAddressExt groupAddress;
	
	public Item(ItemDescriptor itemDescriptor, KnxGroupAddressExt groupAddress) {
		this.itemDescriptor = itemDescriptor;
		this.groupAddress = groupAddress;
	}

	public String getKey()
	{
		return itemDescriptor.getKey();
	}

	public ItemDescriptor getItemDescriptor() {
		return itemDescriptor;
	}

	public String getAddress() {
		return groupAddress.getAddressAsString();
	}

	public String getDescription() {
		return groupAddress.getDescription();
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
		if (!(obj instanceof Item)) {
			return false;
		}
		Item other = (Item) obj;
		return Objects.equals(getAddress(), other.getAddress());
	}

	public String getType() {
		return groupAddress.getDataPointTypeAsString();
	}

	
	
}
