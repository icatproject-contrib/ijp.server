package org.icatproject.ijp.shared;

@SuppressWarnings("serial")
public class ParameterException extends Exception {

	// Needed by GWT serialization
	@SuppressWarnings("unused")
	private ParameterException() {
		super();
	}

	public ParameterException(String msg) {
		super(msg);
	}

}
