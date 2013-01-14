package org.icatproject.ijp_portal.shared;

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
