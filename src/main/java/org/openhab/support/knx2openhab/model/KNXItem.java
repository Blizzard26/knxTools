package org.openhab.support.knx2openhab.model;

import java.util.Map;
import java.util.Objects;

import org.knx.xml.KnxGroupAddressT;

public class KNXItem {

	private final KNXItemDescriptor itemDescriptor;
	private final KnxGroupAddressT groupAddress;
	private final boolean readable;
	private final boolean writeable;

	public KNXItem(KNXItemDescriptor itemDescriptor, KnxGroupAddressT groupAddress, boolean readable,
			boolean writeable) {
		this.itemDescriptor = itemDescriptor;
		this.groupAddress = groupAddress;
		this.readable = readable;
		this.writeable = writeable;
	}

	public String getKey() {
		return itemDescriptor.getKey();
	}

	public KNXItemDescriptor getItemDescriptor() {
		return itemDescriptor;
	}

	public String getAddress() {
		return ModelUtil.getAddressAsString(groupAddress);
	}



	public String getDescription() {
		return groupAddress.getDescription();
	}

	public Map<String, String> getContext() {
		return ModelUtil.getContextFromComment(groupAddress.getComment());
	}
	
	public String getType() {
		return ModelUtil.getDataPointTypeAsString(groupAddress);
	}


	public boolean isReadable() {
		return readable;
	}

	public boolean isWriteable() {
		return writeable;
	}


	@Override
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


}
