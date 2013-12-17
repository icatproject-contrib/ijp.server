package org.icatproject.ijp.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CredType implements IsSerializable {

	public enum Visibility {
		HIDDEN, EXPOSED
	}

	public enum Type {
		STRING
	}

	/* for GWT */
	public CredType() {
	}

	public String getName() {
		return name;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public Type getType() {
		return type;
	}

	private String name;
	private Visibility visibility;
	private Type type;

	public CredType(String name, Visibility visibility, Type type) {
		this.name = name;
		this.visibility = visibility;
		this.type = type;
	}

	@Override
	public String toString() {
		return this.name + " (" + this.type + ")" + " is " + this.visibility;
	}
}
