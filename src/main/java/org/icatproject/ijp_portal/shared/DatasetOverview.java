package org.icatproject.ijp_portal.shared;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DatasetOverview implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long datasetId;
	private String name;
	private String sampleDescription;
	private int numChannels;
	private String users;
	private boolean hasBeads = false;
	private boolean hasDark = false;
	private boolean hasBias = false;
	private boolean hasCheck = false;
	private boolean hasFlatfield = false;
	private boolean hasWhitelight = false;
	private boolean hasEvidenceMaps = false;
	private boolean hasRegErrorMaps = false;
	private Map<String, Object> jobDatasetParameters = new HashMap<String, Object>();
	
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

	public int getNumChannels() {
		return numChannels;
	}

	public void setNumChannels(int numChannels) {
		this.numChannels = numChannels;
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

	public boolean hasBeads() {
		return hasBeads;
	}

	public void setHasBeads(boolean hasBeads) {
		this.hasBeads = hasBeads;
	}

	public boolean hasDark() {
		return hasDark;
	}

	public void setHasDark(boolean hasDark) {
		this.hasDark = hasDark;
	}

	public boolean hasBias() {
		return hasBias;
	}

	public void setHasBias(boolean hasBias) {
		this.hasBias = hasBias;
	}

	public boolean hasCheck() {
		return hasCheck;
	}

	public void setHasCheck(boolean hasCheck) {
		this.hasCheck = hasCheck;
	}

	public boolean hasFlatfield() {
		return hasFlatfield;
	}

	public void setHasFlatfield(boolean hasFlatfield) {
		this.hasFlatfield = hasFlatfield;
	}

	public boolean hasWhitelight() {
		return hasWhitelight;
	}

	public void setHasWhitelight(boolean hasWhitelight) {
		this.hasWhitelight = hasWhitelight;
	}

	public boolean hasEvidenceMaps() {
		return hasEvidenceMaps;
	}

	public void setHasEvidenceMaps(boolean hasEvidenceMaps) {
		this.hasEvidenceMaps = hasEvidenceMaps;
	}

	public boolean hasRegErrorMaps() {
		return hasRegErrorMaps;
	}

	public void setHasRegErrorMaps(boolean hasRegErrorMaps) {
		this.hasRegErrorMaps = hasRegErrorMaps;
	}

	public Map<String, Object> getJobDatasetParameters() {
		return jobDatasetParameters;
	}
	
}
