package org.icatproject.ijp.shared;

import java.io.Serializable;

public class DatasetOverview implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long datasetId;
	private String name;
	private String sampleDescription;
	private String users;
	
	public DatasetOverview() {
		
	}
    
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSampleDescription() {
		return sampleDescription;
	}
	
	public void setSampleDescription(String sampleDescription) {
		this.sampleDescription = sampleDescription;
	}

	public String getUsers() {
		return users;
	}
	
	public void setUsers(String users) {
		this.users = users;
	}

	public Long getDatasetId() {
		return datasetId;
	}

	public void setDatasetId(Long datasetId) {
		this.datasetId = datasetId;
	}

}
