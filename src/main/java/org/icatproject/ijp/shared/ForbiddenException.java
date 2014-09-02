package org.icatproject.ijp.shared;

import java.net.HttpURLConnection;

@SuppressWarnings("serial")
public class ForbiddenException extends IjpException {

	// Needed by GWT serialization
	@SuppressWarnings("unused")
	private ForbiddenException() {
		super();
	}

	public ForbiddenException(String message) {
		super(HttpURLConnection.HTTP_FORBIDDEN, message);
	}

}
