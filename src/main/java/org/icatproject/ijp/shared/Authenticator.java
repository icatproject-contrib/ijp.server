package org.icatproject.ijp.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Authenticator implements IsSerializable {

	/* for GWT */
	public Authenticator() {
	}

	private String authnName;

	public String getAuthnName() {
		return authnName;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public List<CredType> getCredTypes() {
		return credTypes;
	}

	private String friendlyName;
	private List<CredType> credTypes;

	public Authenticator(String authnName, String friendlyName, List<CredType> credTypes) {
		this.authnName = authnName;
		this.friendlyName = friendlyName;
		this.credTypes = credTypes;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(authnName);
		if (!authnName.equals(friendlyName)) {
			sb.append(" (" + friendlyName + ")");
		}
		for (CredType credType : credTypes) {
			sb.append("  " + credType);
		}
		return sb.toString();
	}
}