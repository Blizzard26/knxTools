package org.knx.xml;

public class KnxGroupAddressExt extends KnxGroupAddressT {

	private KnxGroupAddressT groupAddress;
	private String dataPointType;

	public KnxGroupAddressExt(KnxGroupAddressT groupAddress, String dataPointType) {
		this.groupAddress = groupAddress;
		this.dataPointType = dataPointType;
	}

	public String getAddressAsString() {
		long longAddress = getAddress();
		int low = (int) (longAddress % 256);
		longAddress /= 256;
		int middle = (int) (longAddress % 8);
		int high = (int) (longAddress / 8);

		return high + "/" + middle + "/" + low;
	}

	@Override
	public int hashCode() {
		return groupAddress.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return groupAddress.equals(obj);
	}

	@Override
	public String getId() {
		return groupAddress.getId();
	}

	@Override
	public void setId(String value) {
		groupAddress.setId(value);
	}

	@Override
	public long getAddress() {
		return groupAddress.getAddress();
	}

	@Override
	public void setAddress(long value) {
		groupAddress.setAddress(value);
	}

	@Override
	public String getName() {
		return groupAddress.getName();
	}

	@Override
	public void setName(String value) {
		groupAddress.setName(value);
	}

	@Override
	public boolean isUnfiltered() {
		return groupAddress.isUnfiltered();
	}

	@Override
	public void setUnfiltered(Boolean value) {
		groupAddress.setUnfiltered(value);
	}

	@Override
	public boolean isCentral() {
		return groupAddress.isCentral();
	}

	@Override
	public void setCentral(Boolean value) {
		groupAddress.setCentral(value);
	}

	@Override
	public boolean isGlobal() {
		return groupAddress.isGlobal();
	}

	@Override
	public void setGlobal(Boolean value) {
		groupAddress.setGlobal(value);
	}

	@Override
	public String getDatapointType() {
		return groupAddress.getDatapointType();
	}

	@Override
	public void setDatapointType(String value) {
		groupAddress.setDatapointType(value);
	}

	@Override
	public String getDescription() {
		return groupAddress.getDescription();
	}

	@Override
	public void setDescription(String value) {
		groupAddress.setDescription(value);
	}

	@Override
	public String getComment() {
		return groupAddress.getComment();
	}

	@Override
	public void setComment(String value) {
		groupAddress.setComment(value);
	}

	@Override
	public int getPuid() {
		return groupAddress.getPuid();
	}

	@Override
	public void setPuid(int value) {
		groupAddress.setPuid(value);
	}

	@Override
	public String getKey() {
		return groupAddress.getKey();
	}

	@Override
	public void setKey(String value) {
		groupAddress.setKey(value);
	}

	@Override
	public String toString() {
		return groupAddress.toString();
	}

	@Override
	public KnxSecurityModeT getSecurity() {
		return groupAddress.getSecurity();
	}

	@Override
	public void setSecurity(KnxSecurityModeT value) {
		groupAddress.setSecurity(value);
	}

	@Override
	public String getContext() {
		return groupAddress.getContext();
	}

	@Override
	public void setContext(String value) {
		groupAddress.setContext(value);
	}

	public String getDataPointTypeAsString() {
		return dataPointType;
	}

}
