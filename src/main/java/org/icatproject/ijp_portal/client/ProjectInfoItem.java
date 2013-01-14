package org.icatproject.ijp_portal.client;

public class ProjectInfoItem {

	private String name;
	private String value;
	
	public ProjectInfoItem( String name, String value ) {
		this.setName(name);
		this.setValue(value);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
