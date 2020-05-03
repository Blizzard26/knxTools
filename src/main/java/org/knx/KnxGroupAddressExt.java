package org.knx;

public class KnxGroupAddressExt extends KnxGroupAddressT {

	private KnxGroupAddressT groupAddress;
	private String dataPointType;

	public KnxGroupAddressExt(KnxGroupAddressT groupAddress, String dataPointType) {
		this.groupAddress = groupAddress;
		this.dataPointType = dataPointType;
	}
	
	public String getAddressAsString()
	{
		long address = getAddress();
		int low = (int) (address % 256);
		address /= 256;
		int middle = (int) (address % 8);
		int high = (int) (address / 8);
		
		
		return high + "/" + middle + "/" + low;
	}

	public int hashCode() {
		return groupAddress.hashCode();
	}

	public boolean equals(Object obj) {
		return groupAddress.equals(obj);
	}

	public String getId() {
		return groupAddress.getId();
	}

	public void setId(String value) {
		groupAddress.setId(value);
	}

	public long getAddress() {
		return groupAddress.getAddress();
	}

	public void setAddress(long value) {
		groupAddress.setAddress(value);
	}

	public String getName() {
		return groupAddress.getName();
	}

	public void setName(String value) {
		groupAddress.setName(value);
	}

	public boolean isUnfiltered() {
		return groupAddress.isUnfiltered();
	}

	public void setUnfiltered(Boolean value) {
		groupAddress.setUnfiltered(value);
	}

	public boolean isCentral() {
		return groupAddress.isCentral();
	}

	public void setCentral(Boolean value) {
		groupAddress.setCentral(value);
	}

	public boolean isGlobal() {
		return groupAddress.isGlobal();
	}

	public void setGlobal(Boolean value) {
		groupAddress.setGlobal(value);
	}

	public String getDatapointType() {
		return groupAddress.getDatapointType();
	}

	public void setDatapointType(String value) {
		groupAddress.setDatapointType(value);
	}

	public String getDescription() {
		return groupAddress.getDescription();
	}

	public void setDescription(String value) {
		groupAddress.setDescription(value);
	}

	public String getComment() {
		return groupAddress.getComment();
	}

	public void setComment(String value) {
		groupAddress.setComment(value);
	}

	public int getPuid() {
		return groupAddress.getPuid();
	}

	public void setPuid(int value) {
		groupAddress.setPuid(value);
	}

	public String getKey() {
		return groupAddress.getKey();
	}

	public void setKey(String value) {
		groupAddress.setKey(value);
	}

	public String toString() {
		return groupAddress.toString();
	}

	public KnxSecurityModeT getSecurity() {
		return groupAddress.getSecurity();
	}

	public void setSecurity(KnxSecurityModeT value) {
		groupAddress.setSecurity(value);
	}

	public String getContext() {
		return groupAddress.getContext();
	}

	public void setContext(String value) {
		groupAddress.setContext(value);
	}
	
	public String getDataPointTypeAsString()
	{
		return dataPointType;
	}

}
