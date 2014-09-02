package org.icatproject.ijp.shared;

import java.net.HttpURLConnection;

@SuppressWarnings("serial")
public class ParameterException extends IjpException {

	// Needed by GWT serialization
	@SuppressWarnings("unused")
	private ParameterException() {
		super();
	}

	public ParameterException(String message) {
		super(HttpURLConnection.HTTP_BAD_REQUEST, message);
	}

}
