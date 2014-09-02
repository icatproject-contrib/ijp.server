package org.icatproject.ijp.shared;

@SuppressWarnings("serial")
public class IjpException extends Exception {

	// Needed by GWT serialization
	protected IjpException() {
		super();
	}

	private int httpStatusCode;

	public IjpException(int httpStatusCode, String message) {
		super(message);
		this.httpStatusCode = httpStatusCode;
	}

	public int getHttpStatusCode() {
		return httpStatusCode;
	}

}
