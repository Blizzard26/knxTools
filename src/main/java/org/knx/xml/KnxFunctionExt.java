package org.knx.xml;

import java.util.List;

public class KnxFunctionExt extends KnxFunctionT {

	private KnxFunctionT function;
	private KnxSpaceT space;

	public KnxFunctionExt(KnxSpaceT space, KnxFunctionT function) {
		this.space = space;
		this.function = function;
	}

	@Override
	public int hashCode() {
		return function.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return function.equals(obj);
	}

	@Override
	public List<KnxGroupAddressRefT> getGroupAddressRef() {
		return function.getGroupAddressRef();
	}

	@Override
	public String getId() {
		return function.getId();
	}

	@Override
	public void setId(String value) {
		function.setId(value);
	}

	@Override
	public String getName() {
		return function.getName();
	}

	@Override
	public void setName(String value) {
		function.setName(value);
	}

	@Override
	public String getType() {
		return function.getType();
	}

	@Override
	public void setType(String value) {
		function.setType(value);
	}

	@Override
	public List<String> getImplements() {
		return function.getImplements();
	}

	@Override
	public String getNumber() {
		return function.getNumber();
	}

	@Override
	public void setNumber(String value) {
		function.setNumber(value);
	}

	@Override
	public String getComment() {
		return function.getComment();
	}

	@Override
	public void setComment(String value) {
		function.setComment(value);
	}

	@Override
	public String getDescription() {
		return function.getDescription();
	}

	@Override
	public void setDescription(String value) {
		function.setDescription(value);
	}

	@Override
	public KnxCompletionStatusT getCompletionStatus() {
		return function.getCompletionStatus();
	}

	@Override
	public void setCompletionStatus(KnxCompletionStatusT value) {
		function.setCompletionStatus(value);
	}

	@Override
	public String toString() {
		return function.toString();
	}

	@Override
	public String getDefaultGroupRange() {
		return function.getDefaultGroupRange();
	}

	@Override
	public void setDefaultGroupRange(String value) {
		function.setDefaultGroupRange(value);
	}

	@Override
	public int getPuid() {
		return function.getPuid();
	}

	@Override
	public void setPuid(int value) {
		function.setPuid(value);
	}

	@Override
	public String getContext() {
		return function.getContext();
	}

	@Override
	public void setContext(String value) {
		function.setContext(value);
	}

	public KnxSpaceT getSpace() {
		return space;
	}

	public void setSpace(KnxSpaceT space) {
		this.space = space;
	}

}
