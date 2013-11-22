package org.icatproject.ijp.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class AccountDTO implements Serializable {

	private String hostName;
	private String accountName;
	private String password;

	// For JPA
	public AccountDTO() {
	}

	public AccountDTO(String hostName, String accountName, String password) {
		this.hostName = hostName;
		this.accountName = accountName;
		this.password = password;
	}

	public String getHostName() {
		return hostName;
	}

	public String getAccountName() {
		return accountName;
	}

	public String getPassword() {
		return password;
	}

}
