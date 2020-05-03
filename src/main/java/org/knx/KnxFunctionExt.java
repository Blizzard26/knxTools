package org.knx;

import java.util.List;

import org.knx.KnxCompletionStatusT;
import org.knx.KnxFunctionT;
import org.knx.KnxGroupAddressRefT;
import org.knx.KnxSpaceT;

public class KnxFunctionExt extends KnxFunctionT {

	private KnxFunctionT function;
	private KnxSpaceT space;

	public KnxFunctionExt(KnxSpaceT space, KnxFunctionT function) {
		this.space = space;
		this.function = function;
	}

	public int hashCode() {
		return function.hashCode();
	}

	public boolean equals(Object obj) {
		return function.equals(obj);
	}

	public List<KnxGroupAddressRefT> getGroupAddressRef() {
		return function.getGroupAddressRef();
	}

	public String getId() {
		return function.getId();
	}

	public void setId(String value) {
		function.setId(value);
	}

	public String getName() {
		return function.getName();
	}

	public void setName(String value) {
		function.setName(value);
	}

	public String getType() {
		return function.getType();
	}

	public void setType(String value) {
		function.setType(value);
	}

	public List<String> getImplements() {
		return function.getImplements();
	}

	public String getNumber() {
		return function.getNumber();
	}

	public void setNumber(String value) {
		function.setNumber(value);
	}

	public String getComment() {
		return function.getComment();
	}

	public void setComment(String value) {
		function.setComment(value);
	}

	public String getDescription() {
		return function.getDescription();
	}

	public void setDescription(String value) {
		function.setDescription(value);
	}

	public KnxCompletionStatusT getCompletionStatus() {
		return function.getCompletionStatus();
	}

	public void setCompletionStatus(KnxCompletionStatusT value) {
		function.setCompletionStatus(value);
	}

	public String toString() {
		return function.toString();
	}

	public String getDefaultGroupRange() {
		return function.getDefaultGroupRange();
	}

	public void setDefaultGroupRange(String value) {
		function.setDefaultGroupRange(value);
	}

	public int getPuid() {
		return function.getPuid();
	}

	public void setPuid(int value) {
		function.setPuid(value);
	}

	public String getContext() {
		return function.getContext();
	}

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
