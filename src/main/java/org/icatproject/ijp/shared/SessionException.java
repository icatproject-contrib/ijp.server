package org.icatproject.ijp.shared;

import java.net.HttpURLConnection;

@SuppressWarnings("serial")
public class SessionException extends IjpException {

	// Needed by GWT serialization
	@SuppressWarnings("unused")
	private SessionException() {
		super();
	}

	public SessionException(String message) {
		super(HttpURLConnection.HTTP_FORBIDDEN, message);
	}

}
