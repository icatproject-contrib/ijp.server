package org.icatproject.ijp_portal.shared;

@SuppressWarnings("serial")
public class InternalException extends Exception {

	// Needed by GWT serialization
	@SuppressWarnings("unused")
	private InternalException() {
		super();
	}

	public InternalException(String msg) {
		super(msg);
	}

}
