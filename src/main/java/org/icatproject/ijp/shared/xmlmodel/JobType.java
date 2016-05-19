package org.icatproject.ijp.shared.xmlmodel;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.rpc.IsSerializable;

@XmlRootElement
public class JobType implements IsSerializable {

	private String name;
	private String executable;
	private boolean multiple;
	private String type;

	@XmlAttribute
	private String family;
	@XmlAttribute
	private boolean sessionId;
	@XmlAttribute
	private boolean icatUrlRequired;
	@XmlAttribute
	private boolean idsUrlRequired;
	@XmlAttribute
	private boolean acceptsDatasets;
	@XmlAttribute
	private boolean acceptsDatafiles;
	@XmlElement
	private List<String> datasetTypes = new ArrayList<String>();
	@XmlElement
	private List<JobOption> jobOptions = new ArrayList<JobOption>();

	public JobType() {

	}

	public String toString() {
		return this.toJson().toString();
	}
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("name",new JSONString(name));
		json.put("executable",new JSONString(executable));
		json.put("multiple",JSONBoolean.getInstance(multiple));
		json.put("type",new JSONString(type));
		JSONArray datasetTypesArray = new JSONArray();
		int i = 0;
		for( String datasetType : datasetTypes ){
			datasetTypesArray.set( i++, new JSONString(datasetType));
		}
		json.put("datasetTypes", datasetTypesArray);
		JSONArray jobOptionsArray = new JSONArray();
		i = 0;
		for( JobOption jobOption : jobOptions ){
			// Add each JobOption as a map from its name to its content
			jobOptionsArray.set(i++,
					new JSONObject().put(jobOption.getName(), jobOption.toJson())
				);
		}
		return json;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExecutable() {
		return executable;
	}

	public void setExecutable(String executable) {
		this.executable = executable;
	}

	public boolean getMultiple() {
		return multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getDatasetTypes() {
		return datasetTypes;
	}

	public List<JobOption> getJobOptions() {
		return jobOptions;
	}

	public String getFamily() {
		return family;
	}

	public boolean isSessionId() {
		return sessionId;
	}
	
	public boolean isIcatUrlRequired() {
		return icatUrlRequired;
	}

	public boolean isIdsUrlRequired() {
		return idsUrlRequired;
	}
	
	public boolean isAcceptsDatasets(){
		return acceptsDatasets;
	}

	public boolean isAcceptsDatafiles(){
		return acceptsDatafiles;
	}

}
