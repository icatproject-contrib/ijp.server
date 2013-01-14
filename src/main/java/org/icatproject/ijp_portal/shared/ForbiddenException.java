package org.icatproject.ijp_portal.shared;

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
