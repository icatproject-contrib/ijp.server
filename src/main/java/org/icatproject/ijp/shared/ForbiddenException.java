package org.icatproject.ijp.shared;

@SuppressWarnings("serial")
public class ForbiddenException extends Exception {

	// Needed by GWT serialization
	@SuppressWarnings("unused")
	private ForbiddenException() {
		super();
	}

	public ForbiddenException(String msg) {
		super(msg);
	}

}
