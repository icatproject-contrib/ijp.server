package org.icatproject.ijp.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LoginResult implements IsSerializable {

	private String userName;
	private String sessionId;

	/* For GWT */
	public LoginResult() {
	}

	public LoginResult(String sessionId, String userName) {
		this.sessionId = sessionId;
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

	public String getSessionId() {
		return sessionId;
	}

}