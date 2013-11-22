package org.icatproject.ijp.shared.xmlmodel;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ListOption implements IsSerializable {

	private String displayValue;
	private String submitValue;
	
	public ListOption() {
		
	}
	
	public String toString() {
		return "displayValue='" + displayValue + "' submitValue='" + submitValue + "'";
	}
	
	public String getDisplayValue() {
		return displayValue;
	}

	public void setDisplayValue(String displayValue) {
		this.displayValue = displayValue;
	}
	
	public String getSubmitValue() {
		return submitValue;
	}

	public void setSubmitValue(String submitValue) {
		this.submitValue = submitValue;
	}
	
}
