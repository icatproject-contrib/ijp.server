package org.icatproject.ijp.shared.xmlmodel;

import com.google.gwt.user.client.rpc.IsSerializable;

public class JobDatasetParameter implements IsSerializable {

	private String name;
	private String query;
	
	public JobDatasetParameter() {

	}

	public String toString() {
		return "name='" + name + "', query='" + query + "'";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}
