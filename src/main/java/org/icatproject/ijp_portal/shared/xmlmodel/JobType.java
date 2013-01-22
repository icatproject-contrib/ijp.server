package org.icatproject.ijp_portal.shared.xmlmodel;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gwt.user.client.rpc.IsSerializable;

@XmlRootElement
public class JobType implements IsSerializable {

	private String name;
	private String executable;
	private boolean multiple;
	private String type;
	@XmlElement
	private List<String> datasetTypes = new ArrayList<String>();
	@XmlElement
	private List<JobOption> jobOptions = new ArrayList<JobOption>();
	
	public JobType() {
		
	}
	
	public String toString() {
		String lineSep = "\n";
		String objectAsString = "name='" + name + "', executable='" + executable + "', multiple='" + multiple + "', type='" + type + "'" + lineSep;
		objectAsString += "datasetTypes=";
		for (int i=0; i<datasetTypes.size(); i++) {
			objectAsString += "'" + datasetTypes.get(i) + "'";
			if ( i != datasetTypes.size()-1 ) {
				objectAsString += ",";
			}
		}
		objectAsString += lineSep;
		for (JobOption jobOption : jobOptions) {
			objectAsString += jobOption.toString() + lineSep;
		}
		return objectAsString;
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

}
