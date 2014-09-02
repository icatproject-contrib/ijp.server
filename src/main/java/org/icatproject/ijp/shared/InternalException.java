package org.icatproject.ijp.shared;

import java.net.HttpURLConnection;

@SuppressWarnings("serial")
public class InternalException extends IjpException {

	// Needed by GWT serialization
	@SuppressWarnings("unused")
	private InternalException() {
		super();
	}

	public InternalException(String message) {
		super(HttpURLConnection.HTTP_INTERNAL_ERROR, message);
	}

}
